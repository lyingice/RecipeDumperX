package net.mcreator.recipedumperx.api;

public class RecipeDumpException extends Exception {
    public RecipeDumpException() {
        super();
    }

    public RecipeDumpException(String message) {
        super(message);
    }

    // 新增：支持传递异常原因的构造方法，方便调试
    public RecipeDumpException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipeDumpException(Throwable cause) {
        super(cause);
    }
}