package com.j2eefast.flowable.bpm.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j2eefast.common.core.utils.ResponseData;
import com.j2eefast.flowable.bpm.entity.BpmOaFormEntity;
import com.j2eefast.flowable.bpm.entity.Form1Entity;
import com.j2eefast.flowable.bpm.entity.StartProcessInstanceEntity;
import com.j2eefast.flowable.bpm.mapper.Form1Mapper;
import com.j2eefast.flowable.bpm.service.FlowableProcessInstanceService;
import com.j2eefast.flowable.bpm.service.Form1Service;
import com.j2eefast.flowable.bpm.service.impl.BaseProcessService;
import com.j2eefast.framework.utils.UserUtils;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class Form1ServiceImpl extends ServiceImpl<Form1Mapper,Form1Entity> implements Form1Service {
    @Autowired
    private Form1Mapper form1Mapper;

    @Autowired
    @Lazy
    private Form1ServiceImpl form1Service;

    @Autowired
    private FlowableProcessInstanceService flowableProcessInstanceService;

    public boolean add(Form1Entity bpmOaForm){
        return this.save(bpmOaForm);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addForm1(Form1Entity form1Entity) {
        form1Entity.setId(IdUtil.fastSimpleUUID());
        StartProcessInstanceEntity startProcessInstanceVo = new StartProcessInstanceEntity();
        startProcessInstanceVo.setBusinessKey(form1Entity.getId());
        startProcessInstanceVo.setCreator(String.valueOf(UserUtils.getUserId()));
        startProcessInstanceVo.setCurrentUserCode(String.valueOf(UserUtils.getUserId()));
        startProcessInstanceVo.setFormName("form1");
        startProcessInstanceVo.setProcessDefinitionKey("test1");
        startProcessInstanceVo.setVariables(new HashMap<>());
        ResponseData returnStart = flowableProcessInstanceService.startProcessInstanceByKey(startProcessInstanceVo);
        if(returnStart.get("code").equals("00000")) {
            form1Entity.setInstanceId(((ProcessInstance)returnStart.get("data")).getProcessInstanceId());
            return form1Service.add(form1Entity);
        }
        return false;
    }

    @Override
    public Form1Entity getForm1(Long id) {
        return form1Mapper.selectById(id);
    }

    @Override
    public Form1Entity getForm1ByKey(String key) {
        LambdaQueryWrapper<Form1Entity> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(Form1Entity::getId, key);
        return form1Mapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public Boolean update(Form1Entity form1Entity) {
        return form1Mapper.updateById(form1Entity) > 0;
    }
}
