import re

def restore_file(diff_text, output_file):
    lines = diff_text.split('\n')
    restored = []
    in_diff = False
    for line in lines:
        if line.startswith('@@'):
            in_diff = True
            continue
        if not in_diff:
            continue
            
        if line.startswith('-'):
            restored.append(line[1:])
        elif line.startswith(' ') or line == '':
            restored.append(line[1:] if len(line)>0 else "")
        elif line.startswith('+'):
            pass # ignore added
            
    with open(output_file, 'w') as f:
        f.write('\n'.join(restored))

