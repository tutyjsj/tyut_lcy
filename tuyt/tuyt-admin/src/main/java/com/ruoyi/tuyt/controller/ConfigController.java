package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.checkitem.entity.CheckItem;
import com.ruoyi.tuyt.business.checkitem.service.ICheckItemService;
import com.ruoyi.tuyt.business.checktemplate.entity.CheckTemplate;
import com.ruoyi.tuyt.business.checktemplate.service.ICheckTemplateService;
import com.ruoyi.tuyt.business.contact.entity.Contact;
import com.ruoyi.tuyt.business.contact.service.IContactService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ICheckItemService checkItemService;
    private final ICheckTemplateService checkTemplateService;
    private final IContactService contactService;

    // ==================== 检查项 ====================

    @Operation(summary = "获取检查项列表")
    @GetMapping("/check-items")
    public R<PageResult<CheckItem>> checkItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String itemType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkItemService.queryPage(keyword, itemType, pageNum, pageSize));
    }

    @Operation(summary = "新增检查项")
    @PostMapping("/check-items")
    public R<Void> addCheckItem(@RequestBody CheckItem item) {
        if (item.getId() != null) {
            checkItemService.update(item);
        } else {
            checkItemService.add(item);
        }
        R<Void> r = R.ok();
        r.setMessage("保存检查项成功");
        return r;
    }

    @Operation(summary = "删除检查项")
    @DeleteMapping("/check-items/{id}")
    public R<Void> deleteCheckItem(@PathVariable Long id) {
        checkItemService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除检查项成功");
        return r;
    }

    // ==================== 检查模板 ====================

    @Operation(summary = "获取检查模板列表")
    @GetMapping("/check-templates")
    public R<PageResult<CheckTemplate>> checkTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(checkTemplateService.queryPage(keyword, pageNum, pageSize));
    }

    @Operation(summary = "新增/编辑检查模板")
    @PostMapping("/check-templates")
    public R<Void> addCheckTemplate(@RequestBody CheckTemplate template) {
        if (template.getId() != null) {
            checkTemplateService.update(template);
        } else {
            checkTemplateService.add(template);
        }
        R<Void> r = R.ok();
        r.setMessage("保存检查模板成功");
        return r;
    }

    @Operation(summary = "删除检查模板")
    @DeleteMapping("/check-templates/{id}")
    public R<Void> deleteCheckTemplate(@PathVariable Long id) {
        checkTemplateService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除检查模板成功");
        return r;
    }

    // ==================== 通讯录 ====================

    @Operation(summary = "获取通讯录列表")
    @GetMapping("/contacts")
    public R<PageResult<Contact>> contacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String contactType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(contactService.queryPage(keyword, contactType, pageNum, pageSize));
    }

    @Operation(summary = "新增/编辑联系人")
    @PostMapping("/contacts")
    public R<Void> addContact(@RequestBody Contact contact) {
        if (contact.getId() != null) {
            contactService.update(contact);
        } else {
            contactService.add(contact);
        }
        R<Void> r = R.ok();
        r.setMessage(contact.getId() != null ? "编辑联系人成功" : "新增联系人成功");
        return r;
    }

    @Operation(summary = "删除联系人")
    @DeleteMapping("/contacts/{id}")
    public R<Void> deleteContact(@PathVariable Long id) {
        contactService.delete(List.of(id));
        R<Void> r = R.ok();
        r.setMessage("删除联系人成功");
        return r;
    }
}
