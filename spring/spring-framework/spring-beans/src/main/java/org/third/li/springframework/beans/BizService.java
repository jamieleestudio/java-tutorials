package org.third.li.springframework.beans;

public class BizService <T>{

    private T t;

    public void setT(T t) {
        this.t = t;
    }

    public T getT() {
        return t;
    }

}
