package com.j2eefast.flowable.bpm.service;

import com.j2eefast.flowable.bpm.entity.Form1Entity;

public interface Form1Service {
    Boolean addForm1(Form1Entity form1Entity);
    Form1Entity getForm1(Long id);
    Form1Entity getForm1ByKey(String key);
    Boolean update(Form1Entity form1Entity);
}
