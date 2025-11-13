package com.yineng.bpe.rpc.order.api.request;

import java.io.Serializable;


public class RpcRequest implements Serializable{

    //类名
    private String   className;
    //方法名
    private String   methodName;
    //参数
    private Object[] args;
    //类类型
    private Class[]  types;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class[] getTypes() {
        return types;
    }

    public void setTypes(Class[] types) {
        this.types = types;
    }
}
