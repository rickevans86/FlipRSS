#!/usr/bin/env python3
"""Check that every bundled source asset reaches the APK unchanged (including _locales)."""
import argparse
import hashlib
from pathlib import Path
import sys
from zipfile import ZipFile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('apk', type=Path)
    parser.add_argument('--assets', type=Path, default=Path(__file__).resolve().parents[1] / 'app/src/main/assets')
    args = parser.parse_args()
    if not args.assets.is_dir():
        parser.error('Asset directory does not exist')
    failures = []
    count = 0
    with ZipFile(args.apk) as archive:
        names = archive.namelist()
        asset_names = [name for name in names if name.startswith('assets/')]
        if len(asset_names) != len(set(asset_names)):
            failures.append('Duplicate asset paths in APK')
        for source in sorted(args.assets.rglob('*')):
            if not source.is_file():
                continue
            name = 'assets/' + source.relative_to(args.assets).as_posix()
            if name not in asset_names:
                failures.append('Missing: ' + name)
                continue
            with archive.open(name) as packaged, source.open('rb') as original:
                if hashlib.file_digest(packaged, 'sha256').digest() != hashlib.file_digest(original, 'sha256').digest():
                    failures.append('Changed: ' + name)
            count += 1
    if failures:
        print('\n'.join(failures), file=sys.stderr)
        return 1
    print(f'Verified {count} bundled assets, including extension locales, against {args.apk.name}.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
