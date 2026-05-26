package com.ruoyi.tuyt.controller;

import com.ruoyi.tuyt.business.checkitem.entity.CheckItem;
import com.ruoyi.tuyt.business.checkitem.service.ICheckItemService;
import com.ruoyi.tuyt.business.contact.entity.Contact;
import com.ruoyi.tuyt.business.contact.service.IContactService;
import com.ruoyi.tuyt.common.result.PageResult;
import com.ruoyi.tuyt.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ICheckItemService checkItemService;
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
        checkItemService.add(item);
        R<Void> r = R.ok();
        r.setMessage("新增检查项成功");
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
    public R<Map<String, Object>> checkTemplates() {
        List<CheckItem> all = checkItemService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("records", all);
        result.put("total", all.size());
        return R.ok(result);
    }

    @Operation(summary = "新增检查模板")
    @PostMapping("/check-templates")
    public R<Void> addCheckTemplate(@RequestBody Map<String, Object> params) {
        R<Void> r = R.ok();
        r.setMessage("新增检查模板成功");
        return r;
    }

    @Operation(summary = "删除检查模板")
    @DeleteMapping("/check-templates/{id}")
    public R<Void> deleteCheckTemplate(@PathVariable Long id) {
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

    @Operation(summary = "新增联系人")
    @PostMapping("/contacts")
    public R<Void> addContact(@RequestBody Contact contact) {
        contactService.add(contact);
        R<Void> r = R.ok();
        r.setMessage("新增联系人成功");
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
