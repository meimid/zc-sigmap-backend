package com.sigmap.sigmap_paa.service.dto.response;

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
    private String comment;
    private boolean current;

    // Constructeur pour les tâches historiques (terminées)
    public HistoricTaskDto(HistoricTaskInstance historicTaskInstance, List<Comment> taskComments) {
        this.id = historicTaskInstance.getId();
        this.name = historicTaskInstance.getName();
        this.assignee = historicTaskInstance.getAssignee();
        this.startDate = historicTaskInstance.getCreateTime();
        this.endDate = historicTaskInstance.getEndTime();
        this.current = false;

        if (taskComments != null && !taskComments.isEmpty()) {
            this.comment = taskComments.get(taskComments.size() - 1).getFullMessage();
        }
    }

    // Constructeur pour la tâche active (courante)
    public HistoricTaskDto(Task task, List<Comment> taskComments) {
        this.id = task.getId();
        this.name = task.getName();
        this.assignee = task.getAssignee();
        this.startDate = task.getCreateTime();
        this.endDate = null;
        this.current = true;

        if (taskComments != null && !taskComments.isEmpty()) {
            this.comment = taskComments.get(taskComments.size() - 1).getFullMessage();
        }
    }
}
