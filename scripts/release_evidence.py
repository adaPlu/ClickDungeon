from dataclasses import dataclass
from enum import Enum
import json
from pathlib import Path
import re

_SHA = re.compile(r"^[0-9a-f]{40}$")


class GateStatus(Enum):
    PASS = "PASS"
    FAIL = "FAIL"
    BLOCKED = "BLOCKED"


@dataclass(frozen=True)
class GateResult:
    gate: str
    status: GateStatus
    detail: str


class ReleaseEvidenceReport:
    def __init__(self, head_sha: str, gates: list[GateResult]):
        if not _SHA.fullmatch(head_sha):
            raise ValueError("head_sha must be a lowercase 40-character Git SHA")
        self.head_sha = head_sha
        self.gates = tuple(gates)

    @property
    def has_failures(self) -> bool:
        return any(g.status is GateStatus.FAIL for g in self.gates)

    @property
    def is_release_clean(self) -> bool:
        return bool(self.gates) and all(g.status is GateStatus.PASS for g in self.gates)

    def to_dict(self) -> dict:
        return {
            "schema_version": 1,
            "head_sha": self.head_sha,
            "gates": [
                {"gate": g.gate, "status": g.status.value, "detail": g.detail}
                for g in self.gates
            ],
        }

    def write_json(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(self.to_dict(), indent=2, sort_keys=True) + "\n")
