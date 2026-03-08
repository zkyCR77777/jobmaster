from app.core.exceptions import NotFoundError
from app.schemas.contract import ContractClauseList, ContractReport, ContractSummary, ContractTask
from app.services.demo_store import store


class ContractService:
    def create_task(self, file_name: str) -> ContractTask:
        return store.create_contract_task(file_name)

    def summary(self, contract_id: str) -> ContractSummary:
        summary = store.contract_summary(contract_id)
        if summary is None:
            raise NotFoundError('合同解析结果不存在')
        return summary

    def clauses(self, contract_id: str) -> ContractClauseList:
        items = store.contract_clauses(contract_id)
        if items is None:
            raise NotFoundError('合同条款不存在')
        return ContractClauseList(items=items)

    def report(self, contract_id: str) -> ContractReport:
        report = store.contract_report(contract_id)
        if report is None:
            raise NotFoundError('合同报告不存在')
        return report


contract_service = ContractService()
