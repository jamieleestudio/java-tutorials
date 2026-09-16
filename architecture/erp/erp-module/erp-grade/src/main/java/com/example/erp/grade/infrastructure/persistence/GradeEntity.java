package com.example.erp.grade.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "grade")
public class GradeEntity extends BaseEntity {

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "course_name", length = 128, nullable = false)
    private String courseName;

    @Column(name = "score", nullable = false)
    private double score;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}