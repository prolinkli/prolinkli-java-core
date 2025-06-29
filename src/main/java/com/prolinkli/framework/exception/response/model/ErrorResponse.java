package com.prolinkli.framework.exception.response.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends ResponseObject<Void> {
    private int status;
    private String error;        // e.g. “Not Found”
    private String path;
    private String debugMessage; // only if app.debug=true

    public ErrorResponse(int status,
                         String error,
                         String message,
                         String path) {
        // supply the timestamp yourself, then type="ERROR", message, and no data
        super(LocalDateTime.now(), "ERROR", message, null);

        this.status = status;
        this.error  = error;
        this.path   = path;
    }
}