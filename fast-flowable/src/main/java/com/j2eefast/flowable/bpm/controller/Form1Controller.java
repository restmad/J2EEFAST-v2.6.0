/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.j2eefast.flowable.bpm.controller;

import com.j2eefast.common.core.business.annotaion.BussinessLog;
import com.j2eefast.common.core.controller.BaseController;
import com.j2eefast.common.core.enums.BusinessType;
import com.j2eefast.common.core.utils.ResponseData;
import com.j2eefast.common.core.utils.ValidatorUtil;
import com.j2eefast.flowable.bpm.entity.BpmOaFormEntity;
import com.j2eefast.flowable.bpm.entity.BpmTaskFromEntity;
import com.j2eefast.flowable.bpm.entity.Form1Entity;
import com.j2eefast.flowable.bpm.service.BpmOaFormService;
import com.j2eefast.flowable.bpm.service.BpmTaskFromService;
import com.j2eefast.flowable.bpm.service.Form1Service;
import com.j2eefast.framework.annotation.RepeatSubmit;
import com.j2eefast.framework.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 办公请假流程表单代码编写例子
 * @author ZhouZhou
 * @date 2020-04-20 22:19
 */
@Controller
@RequestMapping("/bpm/form1")
@Slf4j
public class Form1Controller extends BaseController{

    private String prefix = "modules/bpm/form1";

    /**
     * 必须注入实例关联表单服务
     */
    @Autowired
    private BpmTaskFromService bpmTaskFromService;

    @Autowired
    private Form1Service form1Service;


    /**
     * 定义关联表单申请表单URL对应此处
     */
    @GetMapping("/add/{id}")
    public String add(@PathVariable("id") Long id, ModelMap mmp){
        log.info("add");
        //通过页面传入的表单ID查询表单关联信息
        BpmTaskFromEntity bpmTaskFrom = bpmTaskFromService.getTaskFromById(id);
        mmp.put("bpmTaskFrom", bpmTaskFrom);
        mmp.put("user", UserUtils.getUserInfo());
        return prefix + "/add";
    }

    /**
     * 表单详情
     * @param taskId
     * @param businessKey
     * @param mmap
     * @return
     */
    @GetMapping("/view")
    public String view(@RequestParam(value="taskId", required=true)String taskId,
                       @RequestParam(value="businessKey", required=true)String businessKey,
                       ModelMap mmap){
        Form1Entity form1 = form1Service.getForm1ByKey(taskId);
        mmap.put("bpmOaForm", form1);
        mmap.put("taskId", taskId);
        return prefix + "/view";
    }


    /**
     * 发起人员撤回
     * @param businessKey
     * @param mmap
     * @return
     */
    @RequiresPermissions("bpm:form:approval")
    @GetMapping("/revoke")
    public String revoke(@RequestParam(value="businessKey", required=true)String businessKey,
                       ModelMap mmap){
        Form1Entity form1 = form1Service.getForm1ByKey(businessKey);
        mmap.put("bpmOaForm", form1);
        return prefix + "/revoke";
    }

    /**
     * 新增
     */
    @RepeatSubmit
    @BussinessLog(title = "OA请假单", businessType = BusinessType.INSERT)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData addBpmOaForm(@Validated Form1Entity form1){
        //校验参数
        ValidatorUtil.validateEntity(form1);
        return ResponseData.success(form1Service.addForm1(form1));
    }



    
        /**
     * 修改
     */
    @GetMapping("/edit")
    public String edit(@RequestParam(value="taskId", required=true)String taskId,
                       @RequestParam(value="businessKey", required=true)String businessKey,
                       ModelMap mmap){
        Form1Entity form1 = form1Service.getForm1ByKey(taskId);
        mmap.put("bpmOaForm", form1);
        mmap.put("taskId", taskId);
        return prefix + "/edit";
    }

    /**
     * 修改保存OA请假单
     */
    @RepeatSubmit
    @RequiresPermissions("bpm:form:edit")
    @BussinessLog(title = "FORM1", businessType = BusinessType.UPDATE)
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData editBpmOaForm(Form1Entity form1){
		ValidatorUtil.validateEntity(form1);
        return form1Service.update(form1)? success(): error("修改失败!");
    }
    
}
