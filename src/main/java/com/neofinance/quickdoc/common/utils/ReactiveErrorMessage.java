package com.neofinance.quickdoc.common.utils;

import com.neofinance.quickdoc.common.QuickDocException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.NoSuchElementException;

public class ReactiveErrorMessage {

    private static final String MSG_NO_CATEGORY = "[Exception class: {0}] 找不到文件分类：{1}";
    private static final String MSG_CATEGORY_CONFLICT = "[Exception class: {0}] 与已有文件分类冲突：{1}";

    private static final String MSG_NO_DIRECTORY = "[Exception class: {0}] 找不到文件目录：{1}";
    private static final String MSG_DIRECTORY_CONFLICT = "[Exception class: {0}] 与已有文件目录冲突：{1}";

    private static final String MSG_NON_NULL_DIRECTORY = "[Exception class: {0}] 文件夹非空：{1}";

    private static final String MSG_NO_FILE = "[Exception class: {0}] 在目录（{1}）找不到文件：{2}";

    public static <T> Mono<T> noCategoryMsg(Object... args) {
        return error(MSG_NO_CATEGORY, NoSuchElementException.class, getCallerClassName(), args);
    }

    public static <T> Mono<T> categoryConflictMsg(Object... args) {
        return error(MSG_CATEGORY_CONFLICT, QuickDocException.class, getCallerClassName(), args);
    }

    public static <T> Mono<T> noDirectoryMsg(Object... args) {
        return error(MSG_NO_DIRECTORY, NoSuchElementException.class, getCallerClassName(), args);
    }

    public static <T> Mono<T> dirConflictMsg(Object... args) {
        return error(MSG_DIRECTORY_CONFLICT, QuickDocException.class, getCallerClassName(), args);
    }

    public static <T> Mono<T> notEmptyDirMsg(Object... args) {
        return error(MSG_NON_NULL_DIRECTORY, QuickDocException.class, getCallerClassName(), args);
    }

    public static <T> Mono<T> fileNotExistMsg(Object... args) {
        return error(MSG_NO_FILE, NoSuchElementException.class, getCallerClassName(), args);
    }

    protected static <T> Mono<T> error(String template, Class clazz, String callerClazz, Object... args) {
        String[] bindArgs = new String[args.length + 1];
        bindArgs[0] = callerClazz;
        for (int i = 1; i <= args.length; i++) {
            bindArgs[i] = args[i - 1].toString();
        }
        String message = MessageFormat.format(template, bindArgs);
        try {
            Constructor ctor = clazz.getDeclaredConstructor(String.class);
            ctor.setAccessible(true);
            Throwable throwable = (Throwable) ctor.newInstance(message);
            return Mono.error(throwable);
        } catch (Exception exp) {
            return Mono.error(exp);
        }
    }

    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReactiveErrorMessage.class.getName())
                    && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                return ste.getClassName();
            }
        }
        return null;
    }

}
