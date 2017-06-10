package jphs.log.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jphs.log.service.ParseLog;

@Controller
@RequestMapping("/parse")
public class ParseLogController {
    @Autowired
    private ParseLog parseLog;

    @RequestMapping("/save")
    public String save() {
        parseLog.readFileByLines();
        return "SUESS";
    }
}
