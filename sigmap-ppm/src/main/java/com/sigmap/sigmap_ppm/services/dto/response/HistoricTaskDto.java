package com.sigmap.sigmap_ppm.services.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HistoricTaskDto {

    private String id;
    private String name;
    private String assignee;
    private Date startDate;
    private Date endDate;
    private String observation;
    private String concern;
    private boolean current;

    // Constructeur pour les tâches historiques (terminées)
    public HistoricTaskDto(HistoricTaskInstance historicTaskInstance) {
        this.id = historicTaskInstance.getId();
        this.name = historicTaskInstance.getName();
        this.assignee = historicTaskInstance.getAssignee();
        this.startDate = historicTaskInstance.getCreateTime();
        this.endDate = historicTaskInstance.getEndTime();
        this.current = false;


    }

    // Constructeur pour la tâche active (courante)
    public HistoricTaskDto(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.assignee = task.getAssignee();
        this.startDate = task.getCreateTime();
        this.endDate = null;
        this.current = true;


    }
}
