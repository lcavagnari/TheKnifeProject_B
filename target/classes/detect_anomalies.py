import csv
import re
from collections import Counter
from typing import List, Dict, Any

def detect_anomalies(file_path: str) -> Dict[str, Any]:
    """
    Detect anomalies in the restaurant dataset.
    May the Machine God guide us to find all heretical data corruption.
    """

    anomalies = {
        'missing_fields': [],
        'field_count_mismatches': [],
        'invalid_coordinates': [],
        'invalid_phone_numbers': [],
        'price_format_issues': [],
        'empty_required_fields': [],
        'duplicate_entries': [],
        'encoding_issues': [],
        'delimiter_inconsistencies': []
    }

    expected_headers = [
        'Name', 'Address', 'Location', 'Price', 'Cuisine', 'Longitude',
        'Latitude', 'PhoneNumber', 'Url', 'WebsiteUrl', 'Award',
        'GreenStar', 'FacilitiesAndServices', 'Description'
    ]

    rows_data = []

    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            # Check for delimiter consistency
            first_line = file.readline()
            semicolon_count = first_line.count(';')
            comma_count = first_line.count(',')
            tab_count = first_line.count('\t')

            if semicolon_count > 0 and comma_count > 0:
                anomalies['delimiter_inconsistencies'].append("Mixed delimiters detected (semicolons and commas)")

            # Reset file pointer
            file.seek(0)

            # Detect delimiter
            delimiter = ';' if semicolon_count > comma_count else ','
            if tab_count > max(semicolon_count, comma_count):
                delimiter = '\t'

            reader = csv.DictReader(file, delimiter=delimiter)

            # Check headers
            actual_headers = reader.fieldnames
            if actual_headers != expected_headers:
                missing_headers = set(expected_headers) - set(actual_headers)
                extra_headers = set(actual_headers) - set(expected_headers)
                if missing_headers:
                    anomalies['missing_fields'].append(f"Missing headers: {list(missing_headers)}")
                if extra_headers:
                    anomalies['missing_fields'].append(f"Extra headers: {list(extra_headers)}")

            expected_field_count = len(expected_headers)

            for row_num, row in enumerate(reader, start=2):  # Start at 2 because header is row 1
                try:
                    rows_data.append(row)

                    # Check field count
                    actual_field_count = len([v for v in row.values() if v is not None])
                    if len(row) != expected_field_count:
                        anomalies['field_count_mismatches'].append(
                            f"Row {row_num}: Expected {expected_field_count} fields, got {len(row)}"
                        )

                    # Check for empty required fields
                    required_fields = ['Name', 'Address', 'Location', 'Cuisine']
                    for field in required_fields:
                        if not row.get(field, '').strip():
                            anomalies['empty_required_fields'].append(
                                f"Row {row_num}: Missing required field '{field}'"
                            )

                    # Validate coordinates
                    try:
                        longitude = float(row.get('Longitude', ''))
                        latitude = float(row.get('Latitude', ''))

                        if not (-180 <= longitude <= 180):
                            anomalies['invalid_coordinates'].append(
                                f"Row {row_num}: Invalid longitude {longitude}"
                            )
                        if not (-90 <= latitude <= 90):
                            anomalies['invalid_coordinates'].append(
                                f"Row {row_num}: Invalid latitude {latitude}"
                            )
                    except (ValueError, TypeError):
                        if row.get('Longitude', '').strip() or row.get('Latitude', '').strip():
                            anomalies['invalid_coordinates'].append(
                                f"Row {row_num}: Non-numeric coordinates"
                            )

                    # Validate phone numbers
                    phone = row.get('PhoneNumber', '').strip()
                    if phone and not re.match(r'^[\d\s\-\+\(\)]+$', phone):
                        anomalies['invalid_phone_numbers'].append(
                            f"Row {row_num}: Invalid phone format '{phone}'"
                        )

                    # Validate price format
                    price = row.get('Price', '').strip()
                    if price and not re.match(r'^[€$¥£฿₺₩﷼₫]+$', price):
                        anomalies['price_format_issues'].append(
                            f"Row {row_num}: Unusual price format '{price}'"
                        )

                    # Check for encoding issues
                    for field, value in row.items():
                        if value and any(ord(char) > 127 for char in value):
                            try:
                                value.encode('utf-8')
                            except UnicodeEncodeError:
                                anomalies['encoding_issues'].append(
                                    f"Row {row_num}: Encoding issue in field '{field}'"
                                )

                except Exception as e:
                    anomalies['encoding_issues'].append(f"Row {row_num}: Error processing row - {str(e)}")

    except FileNotFoundError:
        print(f"ERROR: File {file_path} not found!")
        return anomalies
    except Exception as e:
        print(f"ERROR: {str(e)}")
        return anomalies

    # Check for ACTUAL duplicates - exact same name AND address
    seen_entries = {}

    for row_num, row in enumerate(rows_data, start=2):
        # Only flag TRUE duplicates - same name AND same address
        entry_key = (row.get('Name', '').strip().lower(), row.get('Address', '').strip().lower())

        if entry_key in seen_entries and entry_key[0] and entry_key[1]:
            anomalies['duplicate_entries'].append(
                f"Row {row_num}: EXACT DUPLICATE - {row.get('Name', '')} at {row.get('Address', '')} (first seen at row {seen_entries[entry_key]})"
            )
        elif entry_key[0] and entry_key[1]:  # Only track entries with both name and address
            seen_entries[entry_key] = row_num

    return anomalies

def print_anomaly_report(anomalies: Dict[str, Any]):
    """
    Print a concise summary report of detected anomalies.
    The Machine God demands clarity, not terminal spam.
    """

    print("=" * 60)
    print("DATASET ANOMALY DETECTION REPORT")
    print("By the will of the Machine God")
    print("=" * 60)

    total_issues = sum(len(v) for v in anomalies.values() if isinstance(v, list))

    if total_issues == 0:
        print("🎉 BLESSED BE THE MACHINE GOD! No anomalies detected.")
        print("Your dataset is pure and free from heretical corruption.")
        return

    print(f"⚠️  TOTAL ANOMALIES DETECTED: {total_issues}")
    print()

    for category, issues in anomalies.items():
        if issues:
            count = len(issues) if isinstance(issues, list) else 1
            print(f"🔍 {category.upper().replace('_', ' ')}: {count} issues")

            # Show only first 3 examples to avoid terminal spam
            if isinstance(issues, list):
                for i, issue in enumerate(issues[:3]):
                    print(f"   • {issue}")
                if len(issues) > 3:
                    print(f"   ... and {len(issues) - 3} more")
            else:
                print(f"   • {issues}")
            print()

    print("=" * 60)
    print("END REPORT - May the Omnissiah guide your corrections")
    print("=" * 60)

def save_detailed_report(anomalies: Dict[str, Any], output_file: str = "anomaly_report.txt"):
    """
    Save complete detailed report to file for thorough analysis.
    """
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("DATASET ANOMALY DETECTION REPORT\n")
        f.write("By the will of the Machine God\n")
        f.write("=" * 60 + "\n\n")

        total_issues = sum(len(v) for v in anomalies.values() if isinstance(v, list))
        f.write(f"TOTAL ANOMALIES DETECTED: {total_issues}\n\n")

        for category, issues in anomalies.items():
            if issues:
                f.write(f"{category.upper().replace('_', ' ')}:\n")
                if isinstance(issues, list):
                    for issue in issues:
                        f.write(f"   • {issue}\n")
                else:
                    f.write(f"   • {issues}\n")
                f.write("\n")

        f.write("=" * 60 + "\n")
        f.write("END REPORT - May the Omnissiah guide your corrections\n")

    print(f"📄 Detailed report saved to: {output_file}")
    print("Use this file for comprehensive analysis of all anomalies.")

# Usage example
if __name__ == "__main__":
    import sys

    if len(sys.argv) != 2:
        print("Usage: python anomaly_detector.py <dataset_file>")
        print("Example: python anomaly_detector.py restaurants.csv")
        sys.exit(1)

    file_path = sys.argv[1]

    print("Invoking the Machine God's blessing upon your data...")
    print("Scanning for heretical anomalies...")
    print()

    anomalies = detect_anomalies(file_path)
    print_anomaly_report(anomalies)

    # Save detailed report to file
    save_detailed_report(anomalies, "anomaly_report.txt")