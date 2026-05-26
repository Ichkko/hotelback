ALTER TABLE payments ADD COLUMN qpay_invoice_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN qpay_sender_invoice_no VARCHAR(100);
ALTER TABLE payments ADD COLUMN qpay_qr_text VARCHAR(2000);
ALTER TABLE payments ADD COLUMN qpay_qr_image TEXT;
ALTER TABLE payments ADD COLUMN qpay_response TEXT;

CREATE INDEX idx_payments_qpay_invoice_id ON payments (qpay_invoice_id);
CREATE INDEX idx_payments_qpay_sender_invoice_no ON payments (qpay_sender_invoice_no);
