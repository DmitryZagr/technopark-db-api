package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.mail.park.api.status.ResponseStatus;
import ru.mail.park.service.interfaces.ICommonService;

/**
 * Created by admin on 08.10.16.
 */
@RestController
public class CommonController {

    private final ICommonService commonService;

    @Autowired
    public CommonController(ICommonService iCommonService) {
        this.commonService = iCommonService;
    }

    @RequestMapping(path = "/db/api/clear/", method = RequestMethod.POST,
                                                produces = "application/json")
    public ResponseEntity clear() {
        final int code = commonService.clear();
        final String status = ResponseStatus.getMessage(code,
                ResponseStatus.FORMAT_JSON);
        return ResponseEntity.ok(status);
    }

    @RequestMapping(path = "/db/api/status/", method = RequestMethod.GET,
                                                produces = "application/json")
    public ResponseEntity status() {
        return ResponseEntity.ok(commonService.status());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return
                ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ResponseStatus.FORMAT_JSON
                );
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseBody
    public String resolve404Exception() {
        return
                ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ResponseStatus.FORMAT_JSON
                );
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public String resolve400Exception() {
        return
                ResponseStatus.getMessage(
                        ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ResponseStatus.FORMAT_JSON
                );
    }


}
