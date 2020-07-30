package org.activiti.rest.editor.controller;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/historicProcessInstance")
public class HistoricProcessInstanceController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;

    @RequestMapping("/list")
    public String list(org.springframework.ui.Model model, String deploymentId,String key,Integer page,Integer limit){
        if(page == null){
            page = 1;
        }
        if(limit == null){
            limit = 20;
        }
        HistoricProcessInstanceQuery historicProcessInstanceQuery = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery();
        if(StringUtils.isNotEmpty(deploymentId)){
            historicProcessInstanceQuery.deploymentId(deploymentId);
        }
        if(StringUtils.isNotEmpty(key)){
            historicProcessInstanceQuery.processDefinitionKey(key);
        }
        List<HistoricProcessInstance> list = historicProcessInstanceQuery.listPage((page - 1) * limit,limit);


        model.addAttribute("deploymentId",deploymentId);
        model.addAttribute("total",historicProcessInstanceQuery.count());
        model.addAttribute("key",key);
        model.addAttribute("limit",limit);
        model.addAttribute("page",page);
        model.addAttribute("list",list);
        return "html/historicProcessInstanceList";
    }

//    @RequestMapping("/view")
//    public void view(HttpServletResponse response,
//                     @RequestParam String processInstanceId){
//
//        HistoryService historyService = processEngine.getHistoryService();
//        //processInstanceId
//        //获取历史流程实例
//        HistoricProcessInstance processInstance =   historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
//        //获取流程图
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
//        ProcessEngineConfiguration processEngineConfiguration = processEngine.getProcessEngineConfiguration();
//        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
//
//        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
//
//        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
//        //高亮环节id集合
//        List<String> highLightedActivitis = new ArrayList<String>();
//        //高亮线路id集合
//        List<String> highLightedFlows = getHighLightedFlows(definitionEntity,highLightedActivitList);
//
//        for(HistoricActivityInstance tempActivity : highLightedActivitList){
//            String activityId = tempActivity.getActivityId();
//            highLightedActivitis.add(activityId);
//        }
//
//        //中文显示的是口口口，设置字体就好了
//        InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis,highLightedFlows,"宋体","宋体",null,null,1.0);
//        //单独返回流程图，不高亮显示
////        InputStream imageStream = diagramGenerator.generatePngDiagram(bpmnModel);
//        // 输出资源内容到相应对象
//        byte[] b = new byte[1024];
//        int len;
//        try{
//            while ((len = imageStream.read(b, 0, 1024)) != -1) {
//                response.getOutputStream().write(b, 0, len);
//            }
//        }catch (Exception e){
//
//        }
//
//    }
//
//    private List<String> getHighLightedFlows(
//            ProcessDefinitionEntity processDefinitionEntity,
//            List<HistoricActivityInstance> historicActivityInstances) {
//        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
//        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
//            ActivityImpl activityImpl = processDefinitionEntity
//                    .findActivity(historicActivityInstances.get(i)
//                            .getActivityId());// 得到节点定义的详细信息
//            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
//            ActivityImpl sameActivityImpl1 = processDefinitionEntity
//                    .findActivity(historicActivityInstances.get(i + 1)
//                            .getActivityId());
//            // 将后面第一个节点放在时间相同节点的集合里
//            sameStartTimeNodes.add(sameActivityImpl1);
//            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
//                HistoricActivityInstance activityImpl1 = historicActivityInstances
//                        .get(j);// 后续第一个节点
//                HistoricActivityInstance activityImpl2 = historicActivityInstances
//                        .get(j + 1);// 后续第二个节点
//                if (activityImpl1.getStartTime().equals(
//                        activityImpl2.getStartTime())) {
//                    // 如果第一个节点和第二个节点开始时间相同保存
//                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
//                            .findActivity(activityImpl2.getActivityId());
//                    sameStartTimeNodes.add(sameActivityImpl2);
//                } else {
//                    // 有不相同跳出循环
//                    break;
//                }
//            }
//            List<PvmTransition> pvmTransitions = activityImpl
//                    .getOutgoingTransitions();// 取出节点的所有出去的线
//            for (PvmTransition pvmTransition : pvmTransitions) {
//                // 对所有的线进行遍历
//                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
//                        .getDestination();
//                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
//                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
//                    highFlows.add(pvmTransition.getId());
//                }
//            }
//        }
//        return highFlows;
//    }
}
