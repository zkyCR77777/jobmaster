from __future__ import annotations

from collections.abc import Iterable


def paginate(items: Iterable, page: int, page_size: int) -> tuple[list, int]:
    items_list = list(items)
    total = len(items_list)
    start = (page - 1) * page_size
    end = start + page_size
    return items_list[start:end], total
