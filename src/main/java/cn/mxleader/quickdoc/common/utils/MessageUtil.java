package cn.mxleader.quickdoc.common.utils;

import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;

public class MessageUtil {

    protected static <T> Mono<T> error(String template, Class clazz,
                                       String callerClazz, Object... args) {
        Object[] bindArgs = new Object[args.length + 1];
        bindArgs[0] = callerClazz;
        for (int i = 1; i <= args.length; i++) {
            bindArgs[i] = args[i - 1].toString();
        }
        return error(template, clazz, bindArgs);
    }

    protected static <T> Mono<T> error(String template, Class clazz, Object... args) {
        String message = MessageFormat.format(template, args);
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
            if (!ste.getClassName().equals(MessageUtil.class.getName())
                    && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                return ste.getClassName();
            }
        }
        return null;
    }

}
