USE HomeTechDB;
-- These SSMS examples use literal variables; bind ? parameters in DAO code.
DECLARE @day DATE = CONVERT(DATE, DATEADD(HOUR,7,SYSUTCDATETIME()));
DECLARE @from DATE = DATEFROMPARTS(YEAR(@day), MONTH(@day), 1);
DECLARE @to DATE = DATEADD(MONTH,1,@from); -- exclusive end

-- Daily revenue: net = gross - refunds; top-ups are not product revenue.
SELECT business_date, SUM(recognized_orders) AS recognized_orders,
       SUM(gross_revenue) AS gross_revenue, SUM(refunds) AS refunds,
       SUM(gross_revenue-refunds) AS net_revenue
FROM dbo.vw_HTRevenueEvents
WHERE business_date >= @from AND business_date < @to
GROUP BY business_date ORDER BY business_date;

-- Monthly revenue in a selected year.
SELECT YEAR(business_date) AS [year], MONTH(business_date) AS [month],
       SUM(recognized_orders) AS recognized_orders,
       SUM(gross_revenue) AS gross_revenue, SUM(refunds) AS refunds,
       SUM(gross_revenue-refunds) AS net_revenue
FROM dbo.vw_HTRevenueEvents
WHERE business_date >= DATEFROMPARTS(YEAR(@day),1,1)
  AND business_date < DATEFROMPARTS(YEAR(@day)+1,1,1)
GROUP BY YEAR(business_date), MONTH(business_date)
ORDER BY [year], [month];

-- Workload on the requested delivery date (not the creation date).
SELECT COUNT_BIG(*) AS scheduled_total,
       COALESCE(SUM(CASE WHEN order_status IN ('PENDING','CONFIRMED','SHIPPING') THEN 1 ELSE 0 END),0) AS remaining,
       COALESCE(SUM(CASE WHEN order_status='DELIVERED' THEN 1 ELSE 0 END),0) AS completed,
       COALESCE(SUM(CASE WHEN order_status='CANCELLED' THEN 1 ELSE 0 END),0) AS cancelled
FROM dbo.Orders WHERE desired_delivery_date=@day;
SELECT order_id, recipient_name, recipient_phone, shipping_address, delivery_slot, order_status
FROM dbo.Orders WHERE desired_delivery_date=@day
  AND order_status IN ('PENDING','CONFIRMED','SHIPPING') ORDER BY delivery_slot, order_id;
SELECT COUNT_BIG(*) AS overdue FROM dbo.Orders WHERE desired_delivery_date<@day
  AND order_status IN ('PENDING','CONFIRMED','SHIPPING');
SELECT COUNT_BIG(*) AS unscheduled FROM dbo.Orders WHERE desired_delivery_date IS NULL
  AND order_status IN ('PENDING','CONFIRMED','SHIPPING');

-- Separate cash movements; do not sum cash_in into product revenue.
SELECT business_date, source, SUM(cash_in) AS cash_in, SUM(cash_out) AS cash_out
FROM dbo.vw_HTCashEvents WHERE business_date>=@from AND business_date<@to
GROUP BY business_date, source ORDER BY business_date, source;

-- Legacy-data reports: review before enabling the new dashboard.
SELECT order_id, order_status, delivered_at FROM dbo.Orders
WHERE order_status='DELIVERED' AND delivered_at IS NULL;
SELECT order_id, COUNT_BIG(*) AS payment_rows FROM dbo.Payments
GROUP BY order_id HAVING COUNT_BIG(*)<>1;
SELECT payment_id, order_id, payment_status, paid_at FROM dbo.Payments
WHERE payment_status IN ('PAID','REFUNDED') AND paid_at IS NULL;
-- Never fabricate historical timestamps by filling them with today's date.
