ALTER TABLE [case-management].dbo.case_transaction_sla_hop ADD CONSTRAINT case_transaction_sla_hop_FK FOREIGN KEY (case_id) REFERENCES [case-management].dbo.case_transaction(case_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE [case-management].dbo.case_transaction_comment ADD CONSTRAINT case_transaction_comment_FK FOREIGN KEY (case_id) REFERENCES [case-management].dbo.case_transaction(case_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE [case-management].dbo.case_transaction_document_reference ADD CONSTRAINT case_transaction_document_reference_FK FOREIGN KEY (case_id) REFERENCES [case-management].dbo.case_transaction(case_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE [case-management].dbo.case_transaction_sla_activity ADD CONSTRAINT case_transaction_sla_activity_FK FOREIGN KEY (case_id) REFERENCES [case-management].dbo.case_transaction(case_id) ON DELETE CASCADE ON UPDATE CASCADE;