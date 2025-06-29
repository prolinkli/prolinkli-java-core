package com.prolinkli.framework.exception.response.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject<T> {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    /** SUCCESS, ERROR, WARNING, etc */
    private String type;

    /** user‐facing message */
    private String message;

    /** payload (null on error) */
    private T data;
}
