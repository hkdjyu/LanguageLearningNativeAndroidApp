package edu.cuhk.csci3310project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/*
    * Reference:
    * https://ai.google.dev/gemini-api/docs/get-started/android#java
    */

public class AiMainFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    enum mode {
        FREE, PROOFREADING, SUMMARIZATION
    }

    private static final String TAG = "AiMainFragment";
    private static final String PREFS_NAME = "MessagePrefs";
    private static final String KEY_MESSAGE_COUNT = "MessageCount";
    private static final String DEFAULT_API_KEY = "AIzaSyCtBVpBfa_18IEaNLuSHFK2nY499MVNqHo";

    private String selectedLanguage = "繁體中文";
    private mode currentMode = mode.FREE;
    private GenerativeModelFutures model;
    private ScrollView scrollView;
    private LinearLayout outputContainer;
    private EditText inputText;
    private Button generateButton;
    private Button clearButton;
    private Spinner modeSpinner;
    private List<ChatMessage> messageList;

    private String initialMessage = "";
    private String apiKey = "";

    public AiMainFragment() {
        // Required empty public constructor
    }

    public static AiMainFragment newInstance(String content) {
        AiMainFragment fragment = new AiMainFragment();
        Bundle args = new Bundle();
        args.putString("content", content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialMessage = getArguments().getString("content", "");
            currentMode = mode.PROOFREADING;
        }

        Log.d(TAG, "Initial Message: " + initialMessage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ai_main, container, false);

        messageList = new ArrayList<>();
        setupModel();
        bindViews(rootView);
        setupViews();
        loadMessagesFromPreferences();
        displayMessages();

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // Handle spinner item selection
        switch (i) {
            case 0:
                currentMode = mode.FREE;
                generateButton.setText(R.string.send);
                break;
            case 1:
                currentMode = mode.PROOFREADING;
                generateButton.setText(R.string.check);
                break;
            case 2:
                currentMode = mode.SUMMARIZATION;
                generateButton.setText(R.string.summarize);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void setupModel() {
        // get the API key from the shared preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("settingsPrefs", 0);

        boolean isDevMode = prefs.getBoolean("developerMode", false);
        if (isDevMode) {
            apiKey = DEFAULT_API_KEY;
        } else {
            apiKey = prefs.getString("geminiAPIKey", "");
            if (apiKey.isEmpty()) {
                apiKey = DEFAULT_API_KEY;
            }
        }

        try{
            // Use a model that's applicable for your use case (see "Implement basic use cases" below)
            GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-pro",
                    // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                    /* apiKey */ apiKey);

            // Use the GenerativeModelFutures Java compatibility layer which offers
            // support for ListenableFuture and Publisher APIs
            model = GenerativeModelFutures.from(gm);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.fail_to_setup_model, Toast.LENGTH_SHORT).show();
        }

    }

    private void bindViews(View rootView) {
        scrollView = rootView.findViewById(R.id.aiScrollView);
        outputContainer = rootView.findViewById(R.id.aiContainer);
        inputText = rootView.findViewById(R.id.ai_input_edit_text);
        generateButton = rootView.findViewById(R.id.ai_ask_button);
        clearButton = rootView.findViewById(R.id.ai_clear_button);
        modeSpinner = rootView.findViewById(R.id.ai_spinner);
    }

    private void setupViews() {
        generateButton.setOnClickListener(v -> {
            generateResponse();
        });

        clearButton.setOnClickListener(v -> {
            showClearDialog();
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.ai_mode_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);
        modeSpinner.setOnItemSelectedListener(this);

        if (!initialMessage.isEmpty()) {
            inputText.setText(initialMessage);
            modeSpinner.setSelection(1);
        }
    }

    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.clear_chat);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                outputContainer.removeAllViews();
                messageList.clear();
                clearPreferences();
                outputContainer.removeAllViews();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void generateResponse() {

        if (inputText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), R.string.please_enter_a_message, Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a response to the user's input
        String requestText = inputText.getText().toString();
        // clear the input text
        inputText.setText("");

        // Create previous chat history for context
        ChatFutures chatModel;
        chatModel = getChatModelWithHistory();

        ChatMessage chatMessage = new ChatMessage("user", requestText, String.valueOf(System.currentTimeMillis()));
        messageList.add(chatMessage);
        createMessageView(chatMessage);
        Content.Builder contentBuilder = new Content.Builder();

        // get language from shared preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("settingsPrefs", 0);
        selectedLanguage = prefs.getString("language", "ENGLISH");
        selectedLanguage = Objects.requireNonNull(AppLanguage.fromCode(selectedLanguage)).name();

        String prompt = "Generate a response in " + selectedLanguage + ".\n";

        switch (currentMode) {
            case FREE:
                prompt = "";
                break;
            case PROOFREADING:
                // No response language needed for proofreading
                prompt ="Please proofread the following article for any grammatical errors, typos, or awkward phrasing.\n\n";
                break;
            case SUMMARIZATION:
                prompt = prompt.concat("Your task is to summarize the following article into three points. Avoid technical jargon and explain it in the simplest of words.\n\n");
                break;
        }

        contentBuilder.setRole("user");
        contentBuilder.addText(prompt.concat(requestText));
        Content content = contentBuilder.build();

        // ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        ListenableFuture<GenerateContentResponse> response = chatModel.sendMessage(content);

        // Generate temporary response
        String tempResponse = getString(R.string.ai_generating_response);
        ChatMessage tempChatMessage = new ChatMessage("model", tempResponse, String.valueOf(System.currentTimeMillis()));
        messageList.add(tempChatMessage);
        createMessageView(tempChatMessage);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // remove the temporary response
                messageList.remove(tempChatMessage);
                outputContainer.removeViewAt(outputContainer.getChildCount() - 1);

                // Convert the response to HTML
                String resultHTMLText = simpleMarkdownToHTML(result.getText());
                ChatMessage chatMessage = new ChatMessage("model", resultHTMLText, String.valueOf(System.currentTimeMillis()));
                messageList.add(chatMessage);
                createMessageView(chatMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), R.string.fail_to_generate_response, Toast.LENGTH_SHORT).show();

                // remove the temporary response
                messageList.remove(tempChatMessage);
                outputContainer.removeViewAt(outputContainer.getChildCount() - 1);
            }
        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        });
    }

    private ChatFutures getChatModelWithHistory() {
        // Load chat history to the model
        List<Content> history = new ArrayList<>();

        for (ChatMessage message : messageList) {
            Content.Builder contentBuilder = new Content.Builder();
            contentBuilder.setRole(message.getSender());
            contentBuilder.addText(message.getMessage());
            Content content = contentBuilder.build();
            history.add(content);
        }

        return model.startChat(history);
    }

    private void loadMessagesFromPreferences() {
        // Load messages from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
        int messageCount = prefs.getInt(KEY_MESSAGE_COUNT, 0);

        for (int i = 0; i < messageCount; i++) {
            String sender = prefs.getString("message_sender_" + i, "");
            String message = prefs.getString("message_text_" + i, "");
            String datetime = prefs.getString("message_datetime_" + i, "");

            ChatMessage chatMessage = new ChatMessage(sender, message, datetime);
            messageList.add(chatMessage);
        }

        Log.d(TAG, "Loaded " + messageList.size() + " messages from SharedPreferences.");
    }

    private void saveMessagesToPreferences() {
        // Save messages to SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_MESSAGE_COUNT, messageList.size());

        for (int i = 0; i < messageList.size(); i++) {
            ChatMessage chatMessage = messageList.get(i);
            editor.putString("message_sender_" + i, chatMessage.getSender());
            editor.putString("message_text_" + i, chatMessage.getMessage());
            editor.putString("message_datetime_" + i, chatMessage.getDatetime());
        }

        editor.apply();
        Log.d(TAG, "Saved " + messageList.size() + " messages to SharedPreferences.");
    }

    private void displayMessages() {
        // Display messages in the outputContainer
        if (messageList.isEmpty()) {
            return;
        }
        for (ChatMessage message : messageList) {
            createMessageView(message);
        }
    }

    private void clearPreferences() {
        // Clear all messages from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        messageList.clear();
    }

    private void createMessageView(ChatMessage message) {
        // Create a view for the message
        View messageView;
        TextView messageTextView;
        TextView datetimeTextView;

        if (message.getSender().equals("user")) {
            // Create a view for the user message
            messageView = getLayoutInflater().inflate(R.layout.user_request, outputContainer, false);
            messageTextView = messageView.findViewById(R.id.userRequestContent);
            datetimeTextView = messageView.findViewById(R.id.userRequestDatetime);
        } else {
            // Create a view for the AI message
            messageView = getLayoutInflater().inflate(R.layout.ai_response, outputContainer, false);
            messageTextView = messageView.findViewById(R.id.aiResponseContent);
            datetimeTextView = messageView.findViewById(R.id.aiResponseDatetime);
        }

        messageTextView.setText(HtmlCompat.fromHtml(message.getMessage(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        Date date = new Date(Long.parseLong(message.getDatetime()));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");
        datetimeTextView.setText(formatter.format(date));

        messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // copy the message to the clipboard
                try{
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    String text = "";
                    if (message.getSender().equals("model")) {
                        // convert HTML to plain text
                        text = HtmlCompat.fromHtml(message.getMessage(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                    } else {
                        text = message.getMessage();
                    }

                    ClipData clip = ClipData.newPlainText("simple text", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), R.string.message_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.fail_to_copy_message, Toast.LENGTH_SHORT).show();

                }
            }
        });

        outputContainer.addView(messageView);

        // scroll to the bottom of the ScrollView
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        // Close the keyboard if the message is from the AI
        if (!message.getSender().equals("model")){
            CloseKeyboard();
        }
        saveMessagesToPreferences();
    }

    private String simpleMarkdownToHTML(String markdown) {
        // Convert simple markdown to HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void CloseKeyboard() {
        // Close the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
    }
}