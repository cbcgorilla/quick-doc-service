package cn.mxleader.quickdoc.common.annotation;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthTarget;
import org.bson.types.ObjectId;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreAuth {
    Class field() default ObjectId.class;
    AuthTarget target() default AuthTarget.FOLDER;
    AuthAction action() default AuthAction.READ;
}
