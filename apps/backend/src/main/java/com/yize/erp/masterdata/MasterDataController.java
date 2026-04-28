package com.yize.erp.masterdata;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.masterdata.dto.BusinessCategorySaveRequest;
import com.yize.erp.masterdata.dto.CustomerSaveRequest;
import com.yize.erp.masterdata.dto.LogisticsCompanySaveRequest;
import com.yize.erp.masterdata.dto.ProductAttrSaveRequest;
import com.yize.erp.masterdata.dto.ProductSaveRequest;
import com.yize.erp.masterdata.dto.ProjectSaveRequest;
import com.yize.erp.masterdata.dto.SenderSaveRequest;
import com.yize.erp.masterdata.dto.SupplierSaveRequest;
import com.yize.erp.masterdata.entity.MdBusinessCategory;
import com.yize.erp.masterdata.entity.MdCustomer;
import com.yize.erp.masterdata.entity.MdLogisticsCompany;
import com.yize.erp.masterdata.entity.MdProduct;
import com.yize.erp.masterdata.entity.MdProductAttr;
import com.yize.erp.masterdata.entity.MdProject;
import com.yize.erp.masterdata.entity.MdSender;
import com.yize.erp.masterdata.entity.MdSupplier;
import com.yize.erp.masterdata.mapper.MdBusinessCategoryMapper;
import com.yize.erp.masterdata.mapper.MdCustomerMapper;
import com.yize.erp.masterdata.mapper.MdLogisticsCompanyMapper;
import com.yize.erp.masterdata.mapper.MdProductMapper;
import com.yize.erp.masterdata.mapper.MdProductAttrMapper;
import com.yize.erp.masterdata.mapper.MdProjectMapper;
import com.yize.erp.masterdata.mapper.MdSenderMapper;
import com.yize.erp.masterdata.mapper.MdSupplierMapper;
import com.yize.erp.system.SystemHelper;
import com.yize.erp.system.log.LogOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/master-data")
public class MasterDataController {
    private final MdBusinessCategoryMapper categoryMapper;
    private final MdProductAttrMapper productAttrMapper;
    private final MdProjectMapper projectMapper;
    private final MdProductMapper productMapper;
    private final MdSupplierMapper supplierMapper;
    private final MdCustomerMapper customerMapper;
    private final MdLogisticsCompanyMapper logisticsCompanyMapper;
    private final MdSenderMapper senderMapper;

    public MasterDataController(
            MdBusinessCategoryMapper categoryMapper,
            MdProductAttrMapper productAttrMapper,
            MdProjectMapper projectMapper,
            MdProductMapper productMapper,
            MdSupplierMapper supplierMapper,
            MdCustomerMapper customerMapper,
            MdLogisticsCompanyMapper logisticsCompanyMapper,
            MdSenderMapper senderMapper
    ) {
        this.categoryMapper = categoryMapper;
        this.productAttrMapper = productAttrMapper;
        this.projectMapper = projectMapper;
        this.productMapper = productMapper;
        this.supplierMapper = supplierMapper;
        this.customerMapper = customerMapper;
        this.logisticsCompanyMapper = logisticsCompanyMapper;
        this.senderMapper = senderMapper;
    }

    @GetMapping("/categories")
    public ApiResponse<List<MdBusinessCategory>> categories(@RequestParam String categoryType) {
        return ApiResponse.ok(categoryMapper.selectList(new LambdaQueryWrapper<MdBusinessCategory>()
                .eq(MdBusinessCategory::getCategoryType, categoryType)
                .orderByAsc(MdBusinessCategory::getSortOrder)
                .orderByAsc(MdBusinessCategory::getId)));
    }

    @PostMapping("/categories")
    @LogOperation(module = "基础资料类别", operation = "新增类别")
    public ApiResponse<Void> createCategory(@RequestBody BusinessCategorySaveRequest request) {
        MdBusinessCategory category = new MdBusinessCategory();
        copy(category, request);
        categoryMapper.insert(category);
        return ApiResponse.ok();
    }

    @PutMapping("/categories/{id}")
    @LogOperation(module = "基础资料类别", operation = "编辑类别")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody BusinessCategorySaveRequest request) {
        MdBusinessCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new IllegalArgumentException("类别不存在");
        }
        copy(category, request);
        categoryMapper.updateById(category);
        return ApiResponse.ok();
    }

    @DeleteMapping("/categories/{id}")
    @LogOperation(module = "基础资料类别", operation = "删除类别")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/product-attrs")
    public ApiResponse<PageResult<MdProductAttr>> productAttrs(
            @RequestParam String attrType,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<MdProductAttr> query = new LambdaQueryWrapper<MdProductAttr>()
                .eq(MdProductAttr::getAttrType, attrType)
                .like(keyword != null && !keyword.isBlank(), MdProductAttr::getAttrName, keyword)
                .orderByAsc(MdProductAttr::getSortOrder)
                .orderByDesc(MdProductAttr::getId);
        Page<MdProductAttr> result = productAttrMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/product-attrs")
    @LogOperation(module = "商品属性", operation = "新增商品属性")
    public ApiResponse<Void> createProductAttr(@RequestBody ProductAttrSaveRequest request) {
        MdProductAttr attr = new MdProductAttr();
        copy(attr, request);
        productAttrMapper.insert(attr);
        return ApiResponse.ok();
    }

    @PutMapping("/product-attrs/{id}")
    @LogOperation(module = "商品属性", operation = "编辑商品属性")
    public ApiResponse<Void> updateProductAttr(@PathVariable Long id, @RequestBody ProductAttrSaveRequest request) {
        MdProductAttr attr = productAttrMapper.selectById(id);
        if (attr == null) {
            throw new IllegalArgumentException("商品属性不存在");
        }
        copy(attr, request);
        productAttrMapper.updateById(attr);
        return ApiResponse.ok();
    }

    @DeleteMapping("/product-attrs/{id}")
    @LogOperation(module = "商品属性", operation = "删除商品属性")
    public ApiResponse<Void> deleteProductAttr(@PathVariable Long id) {
        productAttrMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/projects")
    public ApiResponse<PageResult<MdProject>> projects(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String projectCode,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<MdProject> query = new LambdaQueryWrapper<MdProject>()
                .eq(status != null, MdProject::getStatus, status)
                .like(projectCode != null && !projectCode.isBlank(), MdProject::getProjectCode, projectCode)
                .like(projectName != null && !projectName.isBlank(), MdProject::getProjectName, projectName)
                .orderByDesc(MdProject::getId);
        Page<MdProject> result = projectMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/projects")
    @LogOperation(module = "项目信息", operation = "新增项目")
    public ApiResponse<Void> createProject(@RequestBody ProjectSaveRequest request) {
        MdProject project = new MdProject();
        copy(project, request);
        projectMapper.insert(project);
        return ApiResponse.ok();
    }

    @PutMapping("/projects/{id}")
    @LogOperation(module = "项目信息", operation = "编辑项目")
    public ApiResponse<Void> updateProject(@PathVariable Long id, @RequestBody ProjectSaveRequest request) {
        MdProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        copy(project, request);
        projectMapper.updateById(project);
        return ApiResponse.ok();
    }

    @DeleteMapping("/projects/{id}")
    @LogOperation(module = "项目信息", operation = "删除项目")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<MdProduct>> products(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String shelfNo,
            @RequestParam(required = false) String detailStatus,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<MdProduct> query = new LambdaQueryWrapper<MdProduct>()
                .eq(categoryId != null, MdProduct::getCategoryId, categoryId)
                .eq(status != null, MdProduct::getStatus, status)
                .like(shelfNo != null && !shelfNo.isBlank(), MdProduct::getShelfNo, shelfNo)
                .isNotNull("SET".equals(detailStatus), MdProduct::getDetailDescription)
                .isNull("UNSET".equals(detailStatus), MdProduct::getDetailDescription)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(MdProduct::getProductCode, keyword)
                        .or()
                        .like(MdProduct::getProductName, keyword)
                        .or()
                        .like(MdProduct::getMnemonicCode, keyword)
                        .or()
                        .like(MdProduct::getSpecification, keyword))
                .orderByDesc(MdProduct::getId);
        Page<MdProduct> result = productMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/products")
    @LogOperation(module = "商品信息", operation = "新增商品")
    public ApiResponse<Void> createProduct(@RequestBody ProductSaveRequest request) {
        MdProduct product = new MdProduct();
        copy(product, request);
        productMapper.insert(product);
        return ApiResponse.ok();
    }

    @PutMapping("/products/{id}")
    @LogOperation(module = "商品信息", operation = "编辑商品")
    public ApiResponse<Void> updateProduct(@PathVariable Long id, @RequestBody ProductSaveRequest request) {
        MdProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        copy(product, request);
        productMapper.updateById(product);
        return ApiResponse.ok();
    }

    @DeleteMapping("/products/{id}")
    @LogOperation(module = "商品信息", operation = "删除商品")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/suppliers")
    public ApiResponse<PageResult<MdSupplier>> suppliers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<MdSupplier> query = new LambdaQueryWrapper<MdSupplier>()
                .eq(categoryId != null, MdSupplier::getCategoryId, categoryId)
                .eq(status != null, MdSupplier::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(MdSupplier::getSupplierCode, keyword)
                        .or()
                        .like(MdSupplier::getSupplierName, keyword)
                        .or()
                        .like(MdSupplier::getContactName, keyword)
                        .or()
                        .like(MdSupplier::getMobile, keyword))
                .orderByDesc(MdSupplier::getId);
        Page<MdSupplier> result = supplierMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/suppliers")
    @LogOperation(module = "供应商管理", operation = "新增供应商")
    public ApiResponse<Void> createSupplier(@RequestBody SupplierSaveRequest request) {
        MdSupplier supplier = new MdSupplier();
        copy(supplier, request);
        supplierMapper.insert(supplier);
        return ApiResponse.ok();
    }

    @PutMapping("/suppliers/{id}")
    @LogOperation(module = "供应商管理", operation = "编辑供应商")
    public ApiResponse<Void> updateSupplier(@PathVariable Long id, @RequestBody SupplierSaveRequest request) {
        MdSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        copy(supplier, request);
        supplierMapper.updateById(supplier);
        return ApiResponse.ok();
    }

    @DeleteMapping("/suppliers/{id}")
    @LogOperation(module = "供应商管理", operation = "删除供应商")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/customers")
    public ApiResponse<PageResult<MdCustomer>> customers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String salespersonName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<MdCustomer> query = new LambdaQueryWrapper<MdCustomer>()
                .eq(categoryId != null, MdCustomer::getCategoryId, categoryId)
                .eq(status != null, MdCustomer::getStatus, status)
                .like(customerCode != null && !customerCode.isBlank(), MdCustomer::getCustomerCode, customerCode)
                .like(customerName != null && !customerName.isBlank(), MdCustomer::getCustomerName, customerName)
                .like(contactName != null && !contactName.isBlank(), MdCustomer::getContactName, contactName)
                .and(contactPhone != null && !contactPhone.isBlank(), wrapper -> wrapper
                        .like(MdCustomer::getMobile, contactPhone)
                        .or()
                        .like(MdCustomer::getTelephone, contactPhone))
                .like(country != null && !country.isBlank(), MdCustomer::getCountry, country)
                .like(address != null && !address.isBlank(), MdCustomer::getAddress, address)
                .like(salespersonName != null && !salespersonName.isBlank(), MdCustomer::getSalespersonName, salespersonName)
                .orderByDesc(MdCustomer::getId);
        Page<MdCustomer> result = customerMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/customers")
    @LogOperation(module = "客户信息", operation = "新增客户")
    public ApiResponse<Void> createCustomer(@RequestBody CustomerSaveRequest request) {
        MdCustomer customer = new MdCustomer();
        copy(customer, request);
        customerMapper.insert(customer);
        return ApiResponse.ok();
    }

    @PutMapping("/customers/{id}")
    @LogOperation(module = "客户信息", operation = "编辑客户")
    public ApiResponse<Void> updateCustomer(@PathVariable Long id, @RequestBody CustomerSaveRequest request) {
        MdCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new IllegalArgumentException("客户不存在");
        }
        copy(customer, request);
        customerMapper.updateById(customer);
        return ApiResponse.ok();
    }

    @DeleteMapping("/customers/{id}")
    @LogOperation(module = "客户信息", operation = "删除客户")
    public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
        customerMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/logistics")
    public ApiResponse<PageResult<MdLogisticsCompany>> logistics(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String logisticsCode,
            @RequestParam(required = false) String logisticsName
    ) {
        LambdaQueryWrapper<MdLogisticsCompany> query = new LambdaQueryWrapper<MdLogisticsCompany>()
                .like(logisticsCode != null && !logisticsCode.isBlank(), MdLogisticsCompany::getLogisticsCode, logisticsCode)
                .like(logisticsName != null && !logisticsName.isBlank(), MdLogisticsCompany::getLogisticsName, logisticsName)
                .orderByDesc(MdLogisticsCompany::getId);
        Page<MdLogisticsCompany> result = logisticsCompanyMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping("/logistics")
    @LogOperation(module = "物流公司", operation = "新增物流公司")
    public ApiResponse<Void> createLogistics(@RequestBody LogisticsCompanySaveRequest request) {
        MdLogisticsCompany logistics = new MdLogisticsCompany();
        copy(logistics, request);
        logisticsCompanyMapper.insert(logistics);
        return ApiResponse.ok();
    }

    @PutMapping("/logistics/{id}")
    @LogOperation(module = "物流公司", operation = "编辑物流公司")
    public ApiResponse<Void> updateLogistics(@PathVariable Long id, @RequestBody LogisticsCompanySaveRequest request) {
        MdLogisticsCompany logistics = logisticsCompanyMapper.selectById(id);
        if (logistics == null) {
            throw new IllegalArgumentException("物流公司不存在");
        }
        copy(logistics, request);
        logisticsCompanyMapper.updateById(logistics);
        return ApiResponse.ok();
    }

    @DeleteMapping("/logistics/{id}")
    @LogOperation(module = "物流公司", operation = "删除物流公司")
    public ApiResponse<Void> deleteLogistics(@PathVariable Long id) {
        logisticsCompanyMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/logistics/{logisticsId}/senders")
    public ApiResponse<List<MdSender>> senders(@PathVariable Long logisticsId) {
        return ApiResponse.ok(senderMapper.selectList(new LambdaQueryWrapper<MdSender>()
                .eq(MdSender::getLogisticsId, logisticsId)
                .orderByDesc(MdSender::getDefaultFlag)
                .orderByDesc(MdSender::getId)));
    }

    @PostMapping("/logistics/{logisticsId}/senders")
    @LogOperation(module = "物流公司", operation = "新增寄件人")
    public ApiResponse<Void> createSender(@PathVariable Long logisticsId, @RequestBody SenderSaveRequest request) {
        MdSender sender = new MdSender();
        copy(sender, request);
        sender.setLogisticsId(logisticsId);
        senderMapper.insert(sender);
        return ApiResponse.ok();
    }

    private void copy(MdBusinessCategory category, BusinessCategorySaveRequest request) {
        category.setCategoryType(SystemHelper.requireText(request.categoryType(), "类别类型不能为空"));
        category.setParentId(SystemHelper.root(request.parentId()));
        category.setCategoryCode(request.categoryCode());
        category.setCategoryName(SystemHelper.requireText(request.categoryName(), "类别名称不能为空"));
        category.setSortOrder(SystemHelper.zero(request.sortOrder()));
        category.setSaleEnabled(SystemHelper.enabled(request.saleEnabled()));
        category.setPurchaseEnabled(SystemHelper.enabled(request.purchaseEnabled()));
    }

    private void copy(MdProductAttr attr, ProductAttrSaveRequest request) {
        attr.setAttrType(SystemHelper.requireText(request.attrType(), "属性类型不能为空"));
        attr.setAttrName(SystemHelper.requireText(request.attrName(), "名称不能为空"));
        attr.setAllowDecimal(SystemHelper.zero(request.allowDecimal()));
        attr.setSortOrder(SystemHelper.zero(request.sortOrder()));
        attr.setRemark(request.remark());
    }

    private void copy(MdProject project, ProjectSaveRequest request) {
        project.setProjectCode(SystemHelper.requireText(request.projectCode(), "项目编号不能为空"));
        project.setProjectName(SystemHelper.requireText(request.projectName(), "项目名称不能为空"));
        project.setStatus(SystemHelper.enabled(request.status()));
        project.setRemark(request.remark());
    }

    private void copy(MdProduct product, ProductSaveRequest request) {
        product.setProductCode(SystemHelper.requireText(request.productCode(), "商品编号不能为空"));
        product.setProductName(SystemHelper.requireText(request.productName(), "商品名称不能为空"));
        product.setMnemonicCode(request.mnemonicCode());
        product.setCategoryId(request.categoryId());
        product.setBarcode(request.barcode());
        product.setBrandId(request.brandId());
        product.setImageUrl(request.imageUrl());
        product.setSpecification(request.specification());
        product.setSupplierId(request.supplierId());
        product.setShelfNo(request.shelfNo());
        product.setDefaultUnitId(request.defaultUnitId());
        product.setPurchasePrice(request.purchasePrice() == null ? BigDecimal.ZERO : request.purchasePrice());
        product.setCostPrice(request.costPrice() == null ? BigDecimal.ZERO : request.costPrice());
        product.setWholesalePrice(request.wholesalePrice() == null ? BigDecimal.ZERO : request.wholesalePrice());
        product.setRetailPrice(request.retailPrice() == null ? BigDecimal.ZERO : request.retailPrice());
        product.setPurchaseTaxRate(request.purchaseTaxRate() == null ? BigDecimal.ZERO : request.purchaseTaxRate());
        product.setSaleTaxRate(request.saleTaxRate() == null ? BigDecimal.ZERO : request.saleTaxRate());
        product.setMinStock(request.minStock() == null ? BigDecimal.ZERO : request.minStock());
        product.setMaxStock(request.maxStock() == null ? BigDecimal.ZERO : request.maxStock());
        product.setColorNames(request.colorNames());
        product.setSerialEnabled(SystemHelper.enabled(request.serialEnabled()));
        product.setBatchEnabled(SystemHelper.enabled(request.batchEnabled()));
        product.setExpiryEnabled(SystemHelper.enabled(request.expiryEnabled()));
        product.setSaleEnabled(SystemHelper.enabled(request.saleEnabled()));
        product.setPurchaseEnabled(SystemHelper.enabled(request.purchaseEnabled()));
        product.setSelfMadeEnabled(SystemHelper.enabled(request.selfMadeEnabled()));
        product.setOutsourcingEnabled(SystemHelper.enabled(request.outsourcingEnabled()));
        product.setStatus(SystemHelper.enabled(request.status()));
        product.setRemark(request.remark());
        product.setDetailDescription(request.detailDescription());
        product.setProductionDepartment(request.productionDepartment());
    }

    private void copy(MdSupplier supplier, SupplierSaveRequest request) {
        supplier.setSupplierCode(SystemHelper.requireText(request.supplierCode(), "供应商编号不能为空"));
        supplier.setSupplierName(SystemHelper.requireText(request.supplierName(), "供应商名称不能为空"));
        supplier.setCategoryId(request.categoryId());
        supplier.setContactName(request.contactName());
        supplier.setMobile(request.mobile());
        supplier.setEmail(request.email());
        supplier.setTelephone(request.telephone());
        supplier.setAddress(request.address());
        supplier.setPrepaidAmount(request.prepaidAmount() == null ? BigDecimal.ZERO : request.prepaidAmount());
        supplier.setInvoiceEnabled(SystemHelper.zero(request.invoiceEnabled()));
        supplier.setStatus(SystemHelper.enabled(request.status()));
        supplier.setRemark(request.remark());
    }

    private void copy(MdCustomer customer, CustomerSaveRequest request) {
        customer.setCustomerCode(SystemHelper.requireText(request.customerCode(), "客户编号不能为空"));
        customer.setCustomerName(SystemHelper.requireText(request.customerName(), "客户名称不能为空"));
        customer.setCategoryId(request.categoryId());
        customer.setContactName(request.contactName());
        customer.setMobile(request.mobile());
        customer.setTelephone(request.telephone());
        customer.setEmail(request.email());
        customer.setCountry(request.country());
        customer.setAddress(request.address());
        customer.setSalespersonName(request.salespersonName());
        customer.setLogisticsCompany(request.logisticsCompany());
        customer.setLinkedSupplier(request.linkedSupplier());
        customer.setPrepaidAmount(request.prepaidAmount() == null ? BigDecimal.ZERO : request.prepaidAmount());
        customer.setMemberLevel(request.memberLevel());
        customer.setSelfOrderEnabled(SystemHelper.zero(request.selfOrderEnabled()));
        customer.setLoginAccount(request.loginAccount());
        customer.setCreditLimit(request.creditLimit() == null ? BigDecimal.ZERO : request.creditLimit());
        customer.setInvoiceEnabled(SystemHelper.zero(request.invoiceEnabled()));
        customer.setStatus(SystemHelper.enabled(request.status()));
        customer.setTags(request.tags());
        customer.setRemark(request.remark());
        if (customer.getFollowCount() == null) customer.setFollowCount(0);
        if (customer.getTotalDealAmount() == null) customer.setTotalDealAmount(BigDecimal.ZERO);
    }

    private void copy(MdLogisticsCompany logistics, LogisticsCompanySaveRequest request) {
        logistics.setLogisticsCode(SystemHelper.requireText(request.logisticsCode(), "物流编号不能为空"));
        logistics.setLogisticsName(SystemHelper.requireText(request.logisticsName(), "物流名称不能为空"));
        logistics.setExpressType(request.expressType());
        logistics.setExpressCompanyName(request.expressCompanyName());
        logistics.setRemark(request.remark());
    }

    private void copy(MdSender sender, SenderSaveRequest request) {
        sender.setLogisticsId(request.logisticsId());
        sender.setSenderName(SystemHelper.requireText(request.senderName(), "寄件人姓名不能为空"));
        sender.setPhone(request.phone());
        sender.setAddress(request.address());
        sender.setDefaultFlag(SystemHelper.zero(request.defaultFlag()));
    }
}
