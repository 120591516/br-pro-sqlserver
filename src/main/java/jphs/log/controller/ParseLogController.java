package jphs.log.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import jphs.log.service.ParseLogs;
import jphs.log.utils.InterfaceResultUtil;

@Controller
@RequestMapping("/parseLog")
public class ParseLogController {
	@Autowired
	private ParseLogs parseLogs;

	@RequestMapping(value = "/log")
	@ResponseBody
	public JSONObject insertHeipeisUser() {
		JSONObject message = new JSONObject();
		try {
			String fileName = System.getProperty("user.dir") + "\\src\\access_20170601.log";
			parseLogs.readFileByLines();
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
}
