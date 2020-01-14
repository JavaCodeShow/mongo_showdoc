package com.jf.bean;

/**
 * @author 江峰
 * @create 2020-01-10   16:13
 */
public class MyField {
    String name;
    String type;
    String defaultValue;
    boolean required;
    String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "MyField{" + "\r\n" +
                "name=            " + name + "\r\n" +
                "type=            " + type + "\r\n" +
                "defaultValue=    " + defaultValue + "\r\n" +
                "required=        " + required + "\r\n" +
                "comment=         " + comment + "\r\n" +
                "}";
    }
}
