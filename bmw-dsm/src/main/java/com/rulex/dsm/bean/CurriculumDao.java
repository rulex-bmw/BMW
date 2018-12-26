package com.rulex.dsm.bean;

import com.rulex.dsm.pojo.Curriculum;
import org.springframework.stereotype.Repository;


@Repository
public interface CurriculumDao {

    int modifyParam(Curriculum curriculum);

    int modifyPrimary(Curriculum curriculum);

    int delProject(Curriculum curriculum);
}
