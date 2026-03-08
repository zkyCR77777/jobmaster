from app.schemas.delivery import DeliveryPage, DeliveryStats
from app.services.demo_store import paginate, store


class DeliveryService:
    def list_deliveries(
        self,
        page: int,
        page_size: int,
        status_filter: str | None = None,
    ) -> DeliveryPage:
        items = store.list_deliveries()
        if status_filter:
            items = [item for item in items if item.status == status_filter]
        paged, total = paginate(items, page, page_size)
        return DeliveryPage(items=paged, page=page, page_size=page_size, total=total)

    def stats(self) -> DeliveryStats:
        items = store.list_deliveries()
        counter = {item.status: 0 for item in items}
        for item in items:
            counter[item.status] = counter.get(item.status, 0) + 1
        return DeliveryStats(
            total=len(items),
            pending=counter.get('pending', 0),
            delivering=counter.get('delivering', 0),
            delivered=counter.get('delivered', 0),
            viewed=counter.get('viewed', 0),
            written_test=counter.get('written_test', 0),
            interview=counter.get('interview', 0),
            offer=counter.get('offer', 0),
            rejected=counter.get('rejected', 0),
        )


delivery_service = DeliveryService()
