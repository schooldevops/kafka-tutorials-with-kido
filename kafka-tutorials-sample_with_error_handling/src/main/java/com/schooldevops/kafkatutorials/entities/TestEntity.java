package com.schooldevops.kafkatutorials.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {

    private String title;
    private String contents;
    private LocalDateTime time;

}
