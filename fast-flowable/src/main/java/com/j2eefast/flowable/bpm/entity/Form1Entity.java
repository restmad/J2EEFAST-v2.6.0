package com.j2eefast.flowable.bpm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("form1")
public class Form1Entity {
    @TableId(value = "id",type = IdType.INPUT)
    private String id;
    private String data1;
    private String businessKey;
    private String instanceId;
}
