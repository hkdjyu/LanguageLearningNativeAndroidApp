package edu.cuhk.csci3310project;

public enum AppLanguage {
    ENGLISH("en"),
    CHINESE("zh");

    private final String code;

    AppLanguage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AppLanguage fromCode(String code) {
        for (AppLanguage language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return null;
    }

    public static AppLanguage fromName(String name) {
        for (AppLanguage language : values()) {
            if (language.name().equalsIgnoreCase(name)) {
                return language;
            }
        }
        return null;
    }

    public static AppLanguage fromIndex(int index) {
        return values()[index];
    }
}
