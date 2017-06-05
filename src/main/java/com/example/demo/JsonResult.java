package com.example.demo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/6/5.
 */
public class JsonResult {
    private Boolean success = Boolean.valueOf(false);
    private String msg = "";
    private Object obj = null;
    private Integer type = Integer.valueOf(0);
    private Map<String, String> fieldErrors = new HashMap();

    public JsonResult() {
    }

    public JsonResult(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public JsonResult(Boolean success, String msg, Integer type) {
        this.success = success;
        this.msg = msg;
        this.type = type;
    }

    public JsonResult(Boolean success, String msg, Object obj, Integer type) {
        this.success = success;
        this.msg = msg;
        this.obj = obj;
        this.type = type;
    }

    public Boolean getSuccess() {
        return Boolean.valueOf(this.success.booleanValue() && this.fieldErrors.size() == 0);
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return this.obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void addFieldError(String field, String message) {
        this.fieldErrors.put(field, message);
    }

    public void addFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors.putAll(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return this.fieldErrors;
    }

    public static JsonResult resultError(String message) {
        return resultError(message, 0);
    }

    public static JsonResult resultError(String message, int type) {
        return new JsonResult(Boolean.valueOf(false), message, Integer.valueOf(0));
    }

    public static JsonResult resultSuccess(Object obj) {
        return resultSuccess("", obj);
    }

    public static JsonResult resultSuccess(String message, Object obj) {
        return resultSuccess(message, obj, 0);
    }

    public static JsonResult resultSuccess(String message, Object obj, int type) {
        return new JsonResult(Boolean.valueOf(true), message, obj, Integer.valueOf(type));
    }
}
