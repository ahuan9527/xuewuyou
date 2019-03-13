package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,String> {
}
