/* HomeTech extension | SQL Server 2017+ | 2026-11-06
   Base: supplied PRJ301_G4.sql. Run on a backup/test copy first.
   Adds objects; does not recreate HomeTechDB or alter DB connection settings.
   Run the WHOLE file. No GO inside the transaction. Existing base rows stay intact.
   This is a schema migration, not the complete application implementation.
*/
USE HomeTechDB;
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Orders', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Payments', N'U') IS NULL
       OR OBJECT_ID(N'dbo.PasswordResetTokens', N'U') IS NULL
       OR OBJECT_ID(N'dbo.Inventory', N'U') IS NULL
       OR OBJECT_ID(N'dbo.OrderItems', N'U') IS NULL
        THROW 51000, N'Missing base tables from PRJ301_G4.sql.', 1;
    IF COLUMNPROPERTY(OBJECT_ID(N'dbo.Orders'), N'total_amount', 'IsComputed') <> 1
        THROW 51001, N'Expected Orders.total_amount to be computed. Check schema.', 1;

    IF COL_LENGTH(N'dbo.Users', N'auth_version') IS NULL
        ALTER TABLE dbo.Users ADD auth_version INT NOT NULL
            CONSTRAINT DF_Users_AuthVersion DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.Orders', N'desired_delivery_date') IS NULL
        ALTER TABLE dbo.Orders ADD desired_delivery_date DATE NULL;
    IF COL_LENGTH(N'dbo.Orders', N'delivery_slot') IS NULL
        ALTER TABLE dbo.Orders ADD delivery_slot VARCHAR(20) NULL
            CONSTRAINT CK_Orders_DeliverySlot CHECK (delivery_slot IN ('AM','PM','ANY'));
    IF COL_LENGTH(N'dbo.Orders', N'approved_by') IS NULL
        ALTER TABLE dbo.Orders ADD approved_by INT NULL
            CONSTRAINT FK_Orders_ApprovedBy REFERENCES dbo.Users(user_id);
    IF COL_LENGTH(N'dbo.Orders', N'approved_at') IS NULL
        ALTER TABLE dbo.Orders ADD approved_at DATETIME2(0) NULL;
    IF COL_LENGTH(N'dbo.Orders', N'delivered_at') IS NULL
        ALTER TABLE dbo.Orders ADD delivered_at DATETIME2(0) NULL;

    IF COL_LENGTH(N'dbo.Payments', N'transfer_code') IS NULL
        ALTER TABLE dbo.Payments ADD transfer_code VARCHAR(50) NULL;
    IF COL_LENGTH(N'dbo.Payments', N'reported_at') IS NULL
        ALTER TABLE dbo.Payments ADD reported_at DATETIME2(0) NULL;
    IF COL_LENGTH(N'dbo.Payments', N'confirmed_by') IS NULL
        ALTER TABLE dbo.Payments ADD confirmed_by INT NULL
            CONSTRAINT FK_Payments_ConfirmedBy REFERENCES dbo.Users(user_id);
    IF COL_LENGTH(N'dbo.Payments', N'confirmed_at') IS NULL
        ALTER TABLE dbo.Payments ADD confirmed_at DATETIME2(0) NULL;

    IF OBJECT_ID(N'dbo.AdminPermissions', N'U') IS NULL
    CREATE TABLE dbo.AdminPermissions (
        user_id INT NOT NULL REFERENCES dbo.Users(user_id),
        permission_code VARCHAR(30) NOT NULL,
        granted_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_AdminPermissions PRIMARY KEY(user_id, permission_code),
        CONSTRAINT CK_AdminPermissions_Code CHECK (permission_code IN
            ('ORDER_REVIEW','PAYMENT_REVIEW','TOPUP_REVIEW','SERVICE_REVIEW','RETURN_REVIEW','REPORT_VIEW'))
    );
    -- No automatic grants: explicitly select the admin in 02_grant_permissions.sql.

    IF OBJECT_ID(N'dbo.OrderStatusHistory', N'U') IS NULL
    CREATE TABLE dbo.OrderStatusHistory (
        history_id BIGINT IDENTITY PRIMARY KEY,
        order_id BIGINT NOT NULL REFERENCES dbo.Orders(order_id),
        from_status VARCHAR(20) NOT NULL,
        to_status VARCHAR(20) NOT NULL,
        actor_id INT NOT NULL REFERENCES dbo.Users(user_id),
        note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME()
    );

    IF OBJECT_ID(N'dbo.WalletAccounts', N'U') IS NULL
    CREATE TABLE dbo.WalletAccounts (
        user_id INT NOT NULL PRIMARY KEY REFERENCES dbo.Users(user_id),
        balance DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
        updated_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME()
    );
    IF OBJECT_ID(N'dbo.TopUpRequests', N'U') IS NULL
    CREATE TABLE dbo.TopUpRequests (
        topup_id BIGINT IDENTITY PRIMARY KEY,
        user_id INT NOT NULL REFERENCES dbo.Users(user_id),
        amount DECIMAL(18,2) NOT NULL CHECK (amount > 0 AND amount = FLOOR(amount)),
        transfer_code VARCHAR(50) NOT NULL UNIQUE,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
            CHECK (status IN ('PENDING','APPROVED','REJECTED')),
        reported_at DATETIME2(0) NULL,
        reviewed_by INT NULL REFERENCES dbo.Users(user_id),
        reviewed_at DATETIME2(0) NULL,
        review_note NVARCHAR(500) NULL,
        created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME()
    );
    IF OBJECT_ID(N'dbo.WalletLedger', N'U') IS NULL
    CREATE TABLE dbo.WalletLedger (
        entry_id BIGINT IDENTITY PRIMARY KEY,
        user_id INT NOT NULL REFERENCES dbo.WalletAccounts(user_id),
        topup_id BIGINT NOT NULL UNIQUE REFERENCES dbo.TopUpRequests(topup_id),
        amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
        created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME()
    );
    -- Ledger is intentionally top-up-only; wallet spending is a separate extension.

    IF OBJECT_ID(N'dbo.ServicePlans', N'U') IS NULL
    CREATE TABLE dbo.ServicePlans (
        plan_id INT IDENTITY PRIMARY KEY,
        plan_name NVARCHAR(120) NOT NULL,
        description NVARCHAR(1000) NULL,
        price DECIMAL(18,2) NOT NULL CHECK (price > 0 AND price = FLOOR(price)),
        duration_days INT NOT NULL CHECK (duration_days BETWEEN 1 AND 3650),
        is_active BIT NOT NULL DEFAULT 1
    );
    IF OBJECT_ID(N'dbo.ServiceRegistrations', N'U') IS NULL
    CREATE TABLE dbo.ServiceRegistrations (
        registration_id BIGINT IDENTITY PRIMARY KEY,
        user_id INT NOT NULL REFERENCES dbo.Users(user_id),
        plan_id INT NOT NULL REFERENCES dbo.ServicePlans(plan_id),
        plan_name NVARCHAR(120) NOT NULL,
        amount DECIMAL(18,2) NOT NULL CHECK (amount > 0 AND amount = FLOOR(amount)),
        duration_days INT NOT NULL CHECK (duration_days BETWEEN 1 AND 3650),
        transfer_code VARCHAR(50) NOT NULL UNIQUE,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACTIVE','REJECTED')),
        reported_at DATETIME2(0) NULL,
        reviewed_by INT NULL REFERENCES dbo.Users(user_id),
        reviewed_at DATETIME2(0) NULL,
        review_note NVARCHAR(500) NULL,
        starts_at DATETIME2(0) NULL,
        ends_at DATETIME2(0) NULL,
        created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT CK_ServiceRegistration_Dates CHECK
          ((starts_at IS NULL AND ends_at IS NULL) OR
           (starts_at IS NOT NULL AND ends_at IS NOT NULL AND ends_at > starts_at)),
        CONSTRAINT CK_ServiceRegistration_Active CHECK
          (status <> 'ACTIVE' OR (starts_at IS NOT NULL AND ends_at IS NOT NULL))
    );

    IF OBJECT_ID(N'dbo.BankReceipts', N'U') IS NULL
    CREATE TABLE dbo.BankReceipts (
        receipt_id BIGINT IDENTITY PRIMARY KEY,
        -- Canonical bank statement ID, not the customer's transfer memo.
        bank_reference VARCHAR(150) NOT NULL UNIQUE,
        payment_id BIGINT NULL REFERENCES dbo.Payments(payment_id),
        topup_id BIGINT NULL REFERENCES dbo.TopUpRequests(topup_id),
        registration_id BIGINT NULL REFERENCES dbo.ServiceRegistrations(registration_id),
        amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
        received_at DATETIME2(0) NOT NULL,
        recorded_by INT NOT NULL REFERENCES dbo.Users(user_id),
        recorded_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT CK_BankReceipts_Target CHECK
          ((CASE WHEN payment_id IS NULL THEN 0 ELSE 1 END) +
           (CASE WHEN topup_id IS NULL THEN 0 ELSE 1 END) +
           (CASE WHEN registration_id IS NULL THEN 0 ELSE 1 END) = 1)
    );

    IF OBJECT_ID(N'dbo.ReturnRequests', N'U') IS NULL
    CREATE TABLE dbo.ReturnRequests (
        return_id BIGINT IDENTITY PRIMARY KEY,
        order_id BIGINT NOT NULL UNIQUE REFERENCES dbo.Orders(order_id),
        user_id INT NOT NULL REFERENCES dbo.Users(user_id),
        reason NVARCHAR(1000) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED'
            CHECK (status IN ('REQUESTED','APPROVED','REJECTED','RECEIVED','REFUNDED')),
        refund_amount DECIMAL(18,2) NOT NULL CHECK (refund_amount > 0),
        reviewed_by INT NULL REFERENCES dbo.Users(user_id),
        reviewed_at DATETIME2(0) NULL,
        review_note NVARCHAR(500) NULL,
        received_by INT NULL REFERENCES dbo.Users(user_id),
        received_at DATETIME2(0) NULL,
        restocked BIT NOT NULL DEFAULT 0,
        refunded_by INT NULL REFERENCES dbo.Users(user_id),
        refunded_at DATETIME2(0) NULL,
        refund_reference VARCHAR(150) NULL,
        created_at DATETIME2(0) NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT CK_ReturnRequests_Restock CHECK (restocked = 0 OR received_at IS NOT NULL),
        CONSTRAINT CK_ReturnRequests_Refund CHECK
          (status <> 'REFUNDED' OR (refunded_at IS NOT NULL AND refund_reference IS NOT NULL))
    );

    -- Dynamic statements defer binding until newly added columns exist.
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.Payments') AND name='UX_Payments_TransferCode')
        EXEC(N'CREATE UNIQUE INDEX UX_Payments_TransferCode ON dbo.Payments(transfer_code) WHERE transfer_code IS NOT NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.Orders') AND name='IX_Orders_DeliveryDay')
        EXEC(N'CREATE INDEX IX_Orders_DeliveryDay ON dbo.Orders(desired_delivery_date, order_status) INCLUDE (delivery_slot)');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.Payments') AND name='IX_Payments_OrderStatus')
        EXEC(N'CREATE INDEX IX_Payments_OrderStatus ON dbo.Payments(order_id, payment_status) INCLUDE (amount, paid_at)');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.BankReceipts') AND name='UX_BankReceipts_Payment')
        EXEC(N'CREATE UNIQUE INDEX UX_BankReceipts_Payment ON dbo.BankReceipts(payment_id) WHERE payment_id IS NOT NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.BankReceipts') AND name='UX_BankReceipts_TopUp')
        EXEC(N'CREATE UNIQUE INDEX UX_BankReceipts_TopUp ON dbo.BankReceipts(topup_id) WHERE topup_id IS NOT NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.BankReceipts') AND name='UX_BankReceipts_Registration')
        EXEC(N'CREATE UNIQUE INDEX UX_BankReceipts_Registration ON dbo.BankReceipts(registration_id) WHERE registration_id IS NOT NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ReturnRequests') AND name='UX_ReturnRequests_RefundReference')
        EXEC(N'CREATE UNIQUE INDEX UX_ReturnRequests_RefundReference ON dbo.ReturnRequests(refund_reference) WHERE refund_reference IS NOT NULL');
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PasswordResetTokens') AND name='IX_ResetTokens_UserCreated')
        EXEC(N'CREATE INDEX IX_ResetTokens_UserCreated ON dbo.PasswordResetTokens(user_id, created_at DESC)');

    -- Receipts are cash flow, not recognized product revenue. Times stored in UTC.
    EXEC(N'CREATE OR ALTER VIEW dbo.vw_HTCashEvents AS
        SELECT CONVERT(DATE, DATEADD(HOUR,7,b.received_at)) AS business_date,
          CAST(''ORDER'' AS VARCHAR(20)) AS source, b.amount AS cash_in,
          CAST(0 AS DECIMAL(18,2)) AS cash_out
        FROM dbo.BankReceipts b WHERE b.payment_id IS NOT NULL
        UNION ALL
        SELECT CONVERT(DATE, DATEADD(HOUR,7,p.paid_at)), ''COD'', p.amount, CAST(0 AS DECIMAL(18,2))
        FROM dbo.Payments p WHERE p.payment_method=''COD'' AND p.paid_at IS NOT NULL
          AND p.payment_status IN (''PAID'',''REFUNDED'')
        UNION ALL
        SELECT CONVERT(DATE, DATEADD(HOUR,7,b.received_at)),
          CASE WHEN b.topup_id IS NOT NULL THEN ''TOPUP'' ELSE ''SERVICE'' END,
          b.amount, CAST(0 AS DECIMAL(18,2))
        FROM dbo.BankReceipts b WHERE b.payment_id IS NULL
        UNION ALL
        SELECT CONVERT(DATE, DATEADD(HOUR,7,r.refunded_at)), ''RETURN'',
          CAST(0 AS DECIMAL(18,2)), r.refund_amount
        FROM dbo.ReturnRequests r WHERE r.status=''REFUNDED''');

    -- Recognize a new order only when delivered AND fully paid.
    -- Legacy delivered orders with no delivered_at/paid_at are excluded explicitly.
    EXEC(N'CREATE OR ALTER VIEW dbo.vw_HTRevenueEvents AS
        SELECT o.order_id,
          CONVERT(DATE, DATEADD(HOUR,7,
            CASE WHEN o.delivered_at > pay.last_paid_at THEN o.delivered_at ELSE pay.last_paid_at END)) AS business_date,
          o.total_amount AS gross_revenue, CAST(0 AS DECIMAL(18,2)) AS refunds, 1 AS recognized_orders
        FROM dbo.Orders o
        CROSS APPLY (
          SELECT SUM(p.amount) AS received, MAX(p.paid_at) AS last_paid_at
          FROM dbo.Payments p WHERE p.order_id=o.order_id
            AND p.payment_status IN (''PAID'',''REFUNDED'') AND p.paid_at IS NOT NULL
        ) pay
        WHERE o.order_status=''DELIVERED'' AND o.delivered_at IS NOT NULL
          AND pay.last_paid_at IS NOT NULL AND pay.received >= o.total_amount
        UNION ALL
        SELECT r.order_id, CONVERT(DATE,DATEADD(HOUR,7,r.refunded_at)),
          CAST(0 AS DECIMAL(18,2)), r.refund_amount, 0
        FROM dbo.ReturnRequests r JOIN dbo.Orders o ON o.order_id=r.order_id
        WHERE r.status=''REFUNDED'' AND o.delivered_at IS NOT NULL
          AND EXISTS (SELECT 1 FROM dbo.Payments p WHERE p.order_id=o.order_id AND p.paid_at IS NOT NULL)');
    COMMIT;
    PRINT N'HomeTech extension schema installed. Application integration is still required.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
