package org.activiti.rest.editor.controller;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 任务
 */
@Controller
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    //获取受理员任务列表
    @RequestMapping("/list")
    public String list(ModelMap modelMap, String userId, String key, String processInstanceId, String deploymentId, Integer page, Integer limit) {
        List<Task> lists = null;
        if(page == null){
            page = 1;
        }
        if(limit == null){
            limit = 20;
        }
        TaskQuery taskQuery = taskService.createTaskQuery();

        if(StringUtils.isNotEmpty(userId)){
            taskQuery.taskCandidateOrAssigned(userId);
        }
        if(StringUtils.isNotEmpty(key)){
            taskQuery.processDefinitionKey(key);
        }
        if(StringUtils.isNotEmpty(processInstanceId)){
            taskQuery.processInstanceId(processInstanceId);
        }
        if(StringUtils.isNotEmpty(deploymentId)){
            taskQuery.deploymentId(deploymentId);
        }

        lists = taskQuery.orderByTaskCreateTime().desc().listPage((page - 1) * limit,limit);

        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义查询
                .orderByDeploymentId()
                .asc()
                .list();//返回一个集合列表，封装流程定义
        Set<String> set = new HashSet<>();
        for (ProcessDefinition processDefinition:
                list) {
            set.add(processDefinition.getKey());
        }
        modelMap.addAttribute("processDefinitionList",set);
        modelMap.addAttribute("total",taskQuery.count());
        modelMap.addAttribute("limit",limit);
        modelMap.addAttribute("page",page);
        modelMap.addAttribute("tasks",lists);
        modelMap.addAttribute("key",key);
        modelMap.addAttribute("searchKey",key);
        modelMap.addAttribute("userId",userId);
        modelMap.addAttribute("deploymentId",deploymentId);
        modelMap.addAttribute("processInstanceId",processInstanceId);
        return "html/taskList";
    }

    @RequestMapping("/view")
    public void view(HttpServletResponse response,
                     @RequestParam String processInstanceId){
            try {
                InputStream is = getDiagram(processInstanceId);
                if (is == null)
                    return;

                response.setContentType("image/png");

                BufferedImage image = ImageIO.read(is);
                OutputStream out = response.getOutputStream();
                ImageIO.write(image, "png", out);

                is.close();
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        public InputStream getDiagram(String processInstanceId) {
            //获得流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            String processDefinitionId = StringUtils.EMPTY;
            if (processInstance == null) {
                //查询已经结束的流程实例
                HistoricProcessInstance processInstanceHistory =
                        historyService.createHistoricProcessInstanceQuery()
                                .processInstanceId(processInstanceId).singleResult();
                if (processInstanceHistory == null)
                    return null;
                else
                    processDefinitionId = processInstanceHistory.getProcessDefinitionId();
            } else {
                processDefinitionId = processInstance.getProcessDefinitionId();
            }

            //使用宋体
            String fontName = "宋体";
            //获取BPMN模型对象
            BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
            //获取流程实例当前的节点，需要高亮显示
            List<String> currentActs = Collections.EMPTY_LIST;
            if (processInstance != null)
                currentActs = runtimeService.getActiveActivityIds(processInstance.getId());

            return processEngine.getProcessEngineConfiguration()
                    .getProcessDiagramGenerator()
                    .generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                            fontName, fontName, fontName, null, 1.0);
        }

    @RequestMapping("/delete/{id}")
    public @ResponseBody
    String deleteModel(@PathVariable("id")String id){
        taskService.deleteTask(id);
        return "删除成功！";
    }
}
