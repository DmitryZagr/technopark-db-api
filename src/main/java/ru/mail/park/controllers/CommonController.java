package ru.mail.park.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        int code = commonService.clear();
        String status;
        status = ru.mail.park.api.status.ResponseStatus.getMessage(code,
                                        ru.mail.park.api.status.ResponseStatus.FORMAT_JSON);
        return ResponseEntity.ok(status);
    }

    @RequestMapping(path = "/db/api/status/", method = RequestMethod.GET,
                                                produces = "application/json")
    public ResponseEntity status() {
        return ResponseEntity.ok(commonService.status());
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class})
    @ResponseBody
    public String resolveException() {
        return
                ru.mail.park.api.status.ResponseStatus.getMessage(
                        ru.mail.park.api.status.ResponseStatus.ResponceCode.NOT_VALID.ordinal(),
                        ru.mail.park.api.status.ResponseStatus.FORMAT_JSON
                );
    }


}
