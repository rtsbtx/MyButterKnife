package com.demo.mybutterknife.lib.annotation;

public interface Unbinder {

    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() {
        }
    };

}
