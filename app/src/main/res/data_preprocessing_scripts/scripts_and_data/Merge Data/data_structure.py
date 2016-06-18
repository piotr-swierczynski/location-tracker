import json
import os


class File:
    """
    Class to load, save, edit files.
    """
    @staticmethod
    def save_data_as_json(filename, data):
        outputPath = filename
        fout = open(outputPath, 'w')
        fout.write(json.dumps(data))
        fout.close()
        
    @staticmethod
    def save_html(filename, data):
        outputPath = filename
        fout = open(outputPath, 'w')
        fout.write(data.encode('utf8'))
        fout.close()

    @staticmethod
    def load_html(filename):
        html = ""
        with open(filename, 'r') as f:
            for line in f:
                html += line
        return html.decode('utf8')
        
    @staticmethod
    def load_txt(filename):
        html = ""
        with open(filename, 'r') as f:
            for line in f:
                html += line
        return html

    @staticmethod
    def load_info_from_json_file(path):
        database = []
        with open(path, 'r') as f:
            for line in f:
                database.append(line)
        if len(database) > 0:
            return json.loads(database[0])
        else:
            return []


class Dir:
    """
    Class to create, remove directories.
    """
    @staticmethod
    def create_directory(directory):
        if not os.path.exists(directory):
            os.makedirs(directory)