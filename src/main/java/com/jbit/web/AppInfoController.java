package com.jbit.web;

import com.jbit.entity.JsonResult;
import com.jbit.pojo.AppInfo;
import com.jbit.pojo.DevUser;
import com.jbit.service.AppCategoryService;
import com.jbit.service.AppInfoService;
import com.jbit.service.DataDictionaryService;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Appinfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Controller
@RequestMapping("dev/app")
public class AppInfoController {
    @Resource
    private AppInfoService appInfoService;

    @Resource
    private DataDictionaryService dataDictionaryService;

    @Resource
    private AppCategoryService appCategoryService;

    @GetMapping("/apkexist")
    @ResponseBody
    public JsonResult apkexist(String apkname){
        AppInfo appInfo = appInfoService.queryApkexist(apkname);
        if (appInfo == null){
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    /**
     * APP 新增
     * @param appinfo
     * @param a_logoPicPath
     * @return
     */
    @PostMapping("appinfoadd")
    public String appinfoadd(HttpSession session, AppInfo appinfo, MultipartFile a_logoPicPath){
        // 1.实现文件上传
        String server_path = session.getServletContext().getRealPath("/statics/uploadfiles/");
        // 验证大小和图片规格 [未写]
        try {
            a_logoPicPath.transferTo(new File(server_path,a_logoPicPath.getOriginalFilename()));
        } catch (IOException e) {
        }
        // 2.app添加
        DevUser devuser = (DevUser) session.getAttribute("devuser");
        appinfo.setUpdatedate(new Date());
        appinfo.setDevid(devuser.getId());
        appinfo.setCreatedby(devuser.getId());
        appinfo.setCreationdate(new Date());
        appinfo.setLogopicpath("/statics/uploadfiles/"+a_logoPicPath.getOriginalFilename()); // 相对路径
        appinfo.setLogolocpath(server_path+"/"+a_logoPicPath.getOriginalFilename());
        appInfoService.save(appinfo);
        return "redirect:/dev/app/list";
    }

    @RequestMapping("list")
    public String list(HttpSession session, Model model, @RequestParam(defaultValue = "1") Integer pageIndex, String querySoftwareName, Long queryStatus, Long queryFlatformId, Long queryCategoryLevel1, Long queryCategoryLevel2, Long queryCategoryLevel3){
        DevUser devuser = (DevUser) session.getAttribute("devuser");
        // 防止 session丢失
        model.addAttribute("appInfoList",appInfoService.queryAppInfo(pageIndex,devuser.getId(),querySoftwareName,queryStatus,queryFlatformId,queryCategoryLevel1,queryCategoryLevel2,queryCategoryLevel3));
        // 绑定 APP状态
        model.addAttribute("statusList",dataDictionaryService.queryDataByType("APP_STATUS"));
        // 绑定 所属平台
        model.addAttribute("flatFormList",dataDictionaryService.queryDataByType("APP_FLATFORM"));
        // 绑定 一级分类
        model.addAttribute("categoryLevel1List",appCategoryService.queryByParentId(null));
        // 处理 二级分类
        if(queryCategoryLevel1!=null){
            model.addAttribute("categoryLevel2List",appCategoryService.queryByParentId(queryCategoryLevel1));
        }
        // 处理 三级分类
        if(queryCategoryLevel2!=null){
            model.addAttribute("categoryLevel3List",appCategoryService.queryByParentId(queryCategoryLevel2));
        }
        // 参数重新封装传回到页面
        model.addAttribute("querySoftwareName",querySoftwareName);
        model.addAttribute("queryStatus",queryStatus);
        model.addAttribute("queryFlatformId",queryFlatformId);
        model.addAttribute("queryCategoryLevel1",queryCategoryLevel1);
        model.addAttribute("queryCategoryLevel2",queryCategoryLevel2);
        model.addAttribute("queryCategoryLevel3",queryCategoryLevel3);
        return "developer/appinfolist";
    }
}