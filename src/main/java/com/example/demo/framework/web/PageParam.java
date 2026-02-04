package com.example.demo.framework.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {

    private Integer pageNum;
    private Integer pageSize;

}
