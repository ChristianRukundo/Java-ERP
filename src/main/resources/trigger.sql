CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE message_logs
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

CREATE OR REPLACE FUNCTION log_salary_paid_message_erp_function()
RETURNS TRIGGER AS $$
DECLARE
v_employee_firstname VARCHAR(255);
    v_employee_lastname VARCHAR(255);
    v_employee_code VARCHAR(50);
    v_net_salary_amount DECIMAL(19,2);
    v_month_year_text VARCHAR(20);
    v_institution_name VARCHAR(255) := 'Government of Rwanda Institution';
    v_message_text TEXT;
BEGIN
SELECT emp.first_name, emp.last_name, emp.employee_code
INTO v_employee_firstname, v_employee_lastname, v_employee_code
FROM employees emp
WHERE emp.id = NEW.employee_id;

IF NOT FOUND THEN
        v_employee_firstname := 'Valued';
        v_employee_lastname := 'Employee';
        v_employee_code := COALESCE(NEW.employee_id::TEXT, 'N/A');
        RAISE NOTICE 'MessageLogTrigger: Employee details not found for employee_id: %. Using default names.', NEW.employee_id;
END IF;

    v_net_salary_amount := NEW.netsalary;
    v_month_year_text := LPAD(NEW.pay_month::TEXT, 2, '0') || '/' || NEW.pay_year::TEXT;

    v_message_text := 'Dear ' || v_employee_firstname ||
                      ', your salary for ' || v_month_year_text ||
                      ' from ' || v_institution_name ||
                      ' amounting to ' || v_net_salary_amount::TEXT || ' RWF' ||
                      ' has been credited to your account ' || v_employee_code ||
                      ' successfully.';

INSERT INTO message_logs (employee_id, message_content, month_year_context, created_at, updated_at)
VALUES (NEW.employee_id, v_message_text, v_month_year_text, NOW(), NOW());

RAISE NOTICE 'MessageLogTrigger: Message logged for employee_id: %, month/year: %', NEW.employee_id, v_month_year_text;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS salary_paid_message_log_erp_trigger ON payslips;

CREATE TRIGGER salary_paid_message_log_erp_trigger
    AFTER UPDATE OF status ON payslips
    FOR EACH ROW
    WHEN (NEW.status = 'PAID' AND (OLD.status IS NULL OR OLD.status <> 'PAID'))
    EXECUTE FUNCTION log_salary_paid_message_erp_function();