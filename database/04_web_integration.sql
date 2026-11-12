/* Run after 01_extend_hometech.sql. Safe to re-run this migration. */
USE HomeTechDB;
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF OBJECT_ID('dbo.TopUpRequests','U') IS NULL OR OBJECT_ID('dbo.ServiceRegistrations','U') IS NULL
    THROW 51020,N'Run 01_extend_hometech.sql first.',1;
 IF COL_LENGTH('dbo.TopUpRequests','request_key') IS NULL
    ALTER TABLE dbo.TopUpRequests ADD request_key VARCHAR(36) NULL;
 IF COL_LENGTH('dbo.ServiceRegistrations','request_key') IS NULL
    ALTER TABLE dbo.ServiceRegistrations ADD request_key VARCHAR(36) NULL;
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.TopUpRequests') AND name='UX_TopUp_RequestKey')
    EXEC('CREATE UNIQUE INDEX UX_TopUp_RequestKey ON dbo.TopUpRequests(user_id,request_key) WHERE request_key IS NOT NULL');
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.ServiceRegistrations') AND name='UX_Service_RequestKey')
    EXEC('CREATE UNIQUE INDEX UX_Service_RequestKey ON dbo.ServiceRegistrations(user_id,request_key) WHERE request_key IS NOT NULL');
 COMMIT;
 PRINT N'Web integration schema ready.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
