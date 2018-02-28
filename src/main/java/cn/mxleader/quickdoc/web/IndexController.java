package cn.mxleader.quickdoc.web;

import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.FileService;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController {

    private final ReactiveFolderService reactiveFolderService;
    private final ConfigService configService;

    @Autowired
    public IndexController(ReactiveFolderService reactiveFolderService,
                           ConfigService configService) {
        this.reactiveFolderService = reactiveFolderService;
        this.configService = configService;
    }
    @GetMapping()
    public String index(Model model) {
        return "index";
    }

}
