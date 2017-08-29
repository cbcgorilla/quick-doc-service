package com.neofinance.quickdoc.web.mvc;

import com.neofinance.quickdoc.common.entities.FsCategory;
import com.neofinance.quickdoc.service.CategoryService;
import com.neofinance.quickdoc.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class IndexController {

    private final CategoryService categoryService;
    private final DirectoryService directoryFsService;

    @Autowired
    public IndexController(CategoryService categoryService, DirectoryService directoryFsService) {
        this.categoryService = categoryService;
        this.directoryFsService = directoryFsService;
    }

    @ModelAttribute("allCategories")
    public List<FsCategory> allCategories() {
        return categoryService.findAll().toStream().collect(Collectors.toList());
    }

    @GetMapping()
    public String index(Model model) throws IOException {
        model.addAttribute("message", "首页信息。。。。。。。。。。。。。。：");

        model.addAttribute("directories",
                directoryFsService.allRootDirectories()
                        .toStream()
                        //.map(directory -> directory.toString())
                        .collect(Collectors.toList()));
        return "directories";
    }

    @GetMapping("/{directoryId}")
    public String index(@PathVariable Long directoryId, Model model) throws IOException {
        model.addAttribute("message", "首页信息。。。。。。。。。。。。。。：");

        model.addAttribute("directories",
                directoryFsService.findAllByParent(directoryId)
                        .toStream()
                        //.map(directory -> directory.toString())
                        .collect(Collectors.toList()));
        return "directories";
    }

}
