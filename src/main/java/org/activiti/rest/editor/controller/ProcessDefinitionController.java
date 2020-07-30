package org.activiti.rest.editor.controller;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 流程定义表
 */
@Controller
@RequestMapping("/processDefinition")
public class ProcessDefinitionController {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;


    @RequestMapping("/list")
    public String list(org.springframework.ui.Model model, String deploymentId,String processDefinitionId,String processDefinitionKey){


        ProcessDefinitionQuery processDefinitionQuery = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery();
        if(StringUtils.isNotEmpty(deploymentId)){
            processDefinitionQuery.deploymentId(deploymentId);
        }
        if(StringUtils.isNotEmpty(processDefinitionId)){
            processDefinitionQuery.processDefinitionId(processDefinitionId);
        }
        if(StringUtils.isNotEmpty(processDefinitionKey)){
            processDefinitionQuery.processDefinitionKey(processDefinitionKey);
        }


        List<ProcessDefinition> list = processDefinitionQuery
                .orderByProcessDefinitionVersion().desc()//按照版本的升序排列
                .list();//返回一个集合列表，封装流程定义
        model.addAttribute("list",list);
        model.addAttribute("processDefinitionId",processDefinitionId);
        model.addAttribute("deploymentId",deploymentId);
        model.addAttribute("processDefinitionKey",processDefinitionKey);
        return "html/processDefinitionList";

    }

    @RequestMapping("/delete/{deploymentId}")
    public @ResponseBody
    String deleteModel(@PathVariable("deploymentId")String deploymentId){
        processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .deleteDeployment(deploymentId, true);

        return "删除成功！";
    }

    @RequestMapping("/view")
    public void view(HttpServletResponse response,
                     @RequestParam String id){
        //使用宋体
        String fontName = "宋体";
        //获取BPMN模型对象
        BpmnModel model = repositoryService.getBpmnModel(id);
        //获取流程实例当前的节点，需要高亮显示
        List<String> currentActs = Collections.EMPTY_LIST;
        InputStream in = processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                        fontName, fontName, fontName, null, 1.0);
        try{
            if (in == null){
                return;
            }
            response.setContentType("image/png");

            BufferedImage image = ImageIO.read(in);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "png", out);

            in.close();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
