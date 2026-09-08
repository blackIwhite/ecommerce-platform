package com.ecommerce.order.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.order.dto.InvoiceApplyRequest;
import com.ecommerce.order.dto.InvoiceDTO;

public interface InvoiceService {

    void applyInvoice(InvoiceApplyRequest request, Long userId);

    PageResult<InvoiceDTO> listUserInvoices(Long userId, int pageNum, int pageSize);

    InvoiceDTO getInvoice(Long id);

    PageResult<InvoiceDTO> listAdminInvoices(int pageNum, int pageSize, Integer status);

    void issueInvoice(Long id, String invoiceNo, String invoiceUrl);

    void rejectInvoice(Long id, String reason);
}
