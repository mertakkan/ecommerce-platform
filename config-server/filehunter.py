import os
import sys

def compile_files(extensions):
    # Get the directory where the script/exe is located
    if getattr(sys, 'frozen', False):
        # We're running as an executable
        script_dir = os.path.dirname(sys.executable)
    else:
        # We're running as a script
        script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # Create a string for the output filename based on extensions
    ext_string = "_".join([ext.replace('.', '') for ext in extensions])
    output_file = os.path.join(script_dir, f"compiled_files_{ext_string}.txt")
    
    # Counter for found files
    file_count = 0
    
    with open(output_file, 'w', encoding='utf-8') as out_file:
        # Walk through all directories starting from script_dir
        for root, dirs, files in os.walk(script_dir):
            # Skip node_modules directories
            if 'node_modules' in dirs:
                dirs.remove('node_modules')
            
            # Filter files with any of the given extensions
            for file in files:
                if any(file.endswith(ext) for ext in extensions):
                    file_path = os.path.join(root, file)
                    file_count += 1
                    
                    # Skip the output file itself
                    if file_path == output_file:
                        file_count -= 1
                        continue
                    
                    try:
                        # Write file path as a header
                        out_file.write(f"{'='*80}\n")
                        out_file.write(f"FILE: {file_path}\n")
                        out_file.write(f"{'='*80}\n\n")
                        
                        # Read and write file content
                        with open(file_path, 'r', encoding='utf-8', errors='replace') as in_file:
                            out_file.write(in_file.read())
                            out_file.write("\n\n")
                    except Exception as e:
                        out_file.write(f"ERROR: Could not read file {file_path}: {str(e)}\n\n")
    
    return file_count, output_file

def main():
    print("File Content Compiler")
    print("---------------------")
    extension_input = input("Enter file extensions to search for (separate with spaces, e.g. 'vue js css'): ")
    
    # Split the input by spaces and add dots if not present
    extensions = []
    for ext in extension_input.split():
        if not ext.startswith('.'):
            ext = '.' + ext
        extensions.append(ext)
    
    if not extensions:
        print("No extensions provided.")
        input("\nPress Enter to exit...")
        return
    
    print(f"\nSearching for files with extensions: {', '.join(extensions)}")
    print("(Skipping node_modules directories)")
    count, output_path = compile_files(extensions)
    
    if count > 0:
        print(f"\nFound {count} files with extensions {', '.join(extensions)}.")
        print(f"Contents compiled to: {output_path}")
    else:
        print(f"\nNo files with extensions {', '.join(extensions)} found.")
    
    input("\nPress Enter to exit...")

if __name__ == "__main__":
    main()