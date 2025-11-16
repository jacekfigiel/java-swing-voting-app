import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

// ======== MODEL DOMENOWY ========

class Candidate {
    int id;
    String name;
    int votes;

    Candidate(int id, String name) {
        this.id = id;
        this.name = name;
        this.votes = 0;
    }

    @Override
    public String toString() {
        return id + ": " + name + " (Votes: " + votes + ")";
    }
}

class Voter {
    int id;
    String name;
    boolean hasVoted;

    Voter(int id, String name) {
        this.id = id;
        this.name = name;
        this.hasVoted = false;
    }

    @Override
    public String toString() {
        return id + ": " + name + " (Has voted: " + (hasVoted ? "Yes" : "No") + ")";
    }
}

// ======== REPOZYTORIA ========

interface CandidateRepository {
    void add(Candidate c);
    List<Candidate> findAll();
    Candidate findById(int id);
    void resetAllVotes();
}

interface VoterRepository {
    void add(Voter v);
    List<Voter> findAll();
    Voter findById(int id);
    void resetAllVoters();
}

class InMemoryCandidateRepository implements CandidateRepository {
    private final List<Candidate> candidates = new ArrayList<>();

    @Override
    public void add(Candidate c) {
        candidates.add(c);
    }

    @Override
    public List<Candidate> findAll() {
        return candidates;
    }

    @Override
    public Candidate findById(int id) {
        return candidates.stream()
                .filter(c -> c.id == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void resetAllVotes() {
        for (Candidate c : candidates) {
            c.votes = 0;
        }
    }
}

class InMemoryVoterRepository implements VoterRepository {
    private final List<Voter> voters = new ArrayList<>();

    @Override
    public void add(Voter v) {
        voters.add(v);
    }

    @Override
    public List<Voter> findAll() {
        return voters;
    }

    @Override
    public Voter findById(int id) {
        return voters.stream()
                .filter(v -> v.id == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void resetAllVoters() {
        for (Voter v : voters) {
            v.hasVoted = false;
        }
    }
}

// ======== SERWIS GŁOSOWANIA ========

class VotingService {
    private final CandidateRepository candidateRepository;
    private final VoterRepository voterRepository;

    VotingService(CandidateRepository candidateRepository, VoterRepository voterRepository) {
        this.candidateRepository = candidateRepository;
        this.voterRepository = voterRepository;
    }

    public void castVote(Candidate candidate, Voter voter) {
        if (candidate == null || voter == null) {
            throw new IllegalArgumentException("Candidate and voter must be selected");
        }
        if (voter.hasVoted) {
            throw new IllegalStateException("Voter has already voted");
        }
        candidate.votes++;
        voter.hasVoted = true;
    }

    public void resetElection() {
        candidateRepository.resetAllVotes();
        voterRepository.resetAllVoters();
    }
}

// ======== TABLE MODELE DO JTable ========

class CandidateTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Votes"};
    private final CandidateRepository candidateRepository;

    CandidateTableModel(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public int getRowCount() {
        return candidateRepository.findAll().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Candidate c = candidateRepository.findAll().get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.id;
            case 1 -> c.name;
            case 2 -> c.votes;
            default -> null;
        };
    }
}

class VoterTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Has voted"};
    private final VoterRepository voterRepository;

    VoterTableModel(VoterRepository voterRepository) {
        this.voterRepository = voterRepository;
    }

    @Override
    public int getRowCount() {
        return voterRepository.findAll().size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Voter v = voterRepository.findAll().get(rowIndex);
        return switch (columnIndex) {
            case 0 -> v.id;
            case 1 -> v.name;
            case 2 -> v.hasVoted ? "Yes" : "No";
            default -> null;
        };
    }
}

// ======== GŁÓWNE GUI ========

public class VotingAppGUI {
    private JFrame frame;

    private final CandidateRepository candidateRepository = new InMemoryCandidateRepository();
    private final VoterRepository voterRepository = new InMemoryVoterRepository();
    private final VotingService votingService = new VotingService(candidateRepository, voterRepository);

    private CandidateTableModel candidateTableModel;
    private VoterTableModel voterTableModel;

    private JTable candidateTable;
    private JTable voterTable;

    private JComboBox<Candidate> candidateComboBox;
    private JComboBox<Voter> voterComboBox;

    private int candidateId = 1;
    private int voterId = 1;

    public VotingAppGUI() {
        initUI();
    }

    private void initUI() {
        frame = new JFrame("Voting App (Improved)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 500);
        frame.setLayout(new BorderLayout());

        // Modele tabel
        candidateTableModel = new CandidateTableModel(candidateRepository);
        voterTableModel = new VoterTableModel(voterRepository);

        // Tabele
        candidateTable = new JTable(candidateTableModel);
        voterTable = new JTable(voterTableModel);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // Panel kandydatów
        JPanel candidatePanel = new JPanel(new BorderLayout());
        candidatePanel.setBorder(BorderFactory.createTitledBorder("Candidates"));

        candidatePanel.add(new JScrollPane(candidateTable), BorderLayout.CENTER);

        JPanel candidateInputPanel = new JPanel(new BorderLayout());
        JTextField candidateNameField = new JTextField();
        JButton addCandidateButton = new JButton("Add Candidate");
        addCandidateButton.addActionListener(e -> addCandidate(candidateNameField));
        candidateInputPanel.add(candidateNameField, BorderLayout.CENTER);
        candidateInputPanel.add(addCandidateButton, BorderLayout.EAST);

        candidatePanel.add(candidateInputPanel, BorderLayout.SOUTH);

        // Panel wyborców
        JPanel voterPanel = new JPanel(new BorderLayout());
        voterPanel.setBorder(BorderFactory.createTitledBorder("Voters"));

        voterPanel.add(new JScrollPane(voterTable), BorderLayout.CENTER);

        JPanel voterInputPanel = new JPanel(new BorderLayout());
        JTextField voterNameField = new JTextField();
        JButton addVoterButton = new JButton("Add Voter");
        addVoterButton.addActionListener(e -> addVoter(voterNameField));
        voterInputPanel.add(voterNameField, BorderLayout.CENTER);
        voterInputPanel.add(addVoterButton, BorderLayout.EAST);

        voterPanel.add(voterInputPanel, BorderLayout.SOUTH);

        mainPanel.add(candidatePanel);
        mainPanel.add(voterPanel);

        // Panel głosowania (dropdowny + przyciski)
        JPanel votePanel = new JPanel();
        votePanel.setBorder(BorderFactory.createTitledBorder("Cast Vote"));

        candidateComboBox = new JComboBox<>();
        voterComboBox = new JComboBox<>();

        JButton castVoteButton = new JButton("Cast Vote");
        castVoteButton.addActionListener(this::handleCastVote);

        JButton resetButton = new JButton("Reset Election");
        resetButton.addActionListener(this::handleReset);

        votePanel.add(new JLabel("Candidate:"));
        votePanel.add(candidateComboBox);
        votePanel.add(new JLabel("Voter:"));
        votePanel.add(voterComboBox);
        votePanel.add(castVoteButton);
        votePanel.add(resetButton);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(votePanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addCandidate(JTextField candidateNameField) {
        String name = candidateNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Candidate name cannot be empty.");
            return;
        }
        Candidate c = new Candidate(candidateId++, name);
        candidateRepository.add(c);
        candidateNameField.setText("");

        refreshCandidateTable();
        refreshCandidateComboBox();
    }

    private void addVoter(JTextField voterNameField) {
        String name = voterNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Voter name cannot be empty.");
            return;
        }
        Voter v = new Voter(voterId++, name);
        voterRepository.add(v);
        voterNameField.setText("");

        refreshVoterTable();
        refreshVoterComboBox();
    }

    private void handleCastVote(ActionEvent e) {
        Candidate selectedCandidate = (Candidate) candidateComboBox.getSelectedItem();
        Voter selectedVoter = (Voter) voterComboBox.getSelectedItem();

        if (selectedCandidate == null || selectedVoter == null) {
            JOptionPane.showMessageDialog(frame, "Select a candidate and a voter.");
            return;
        }

        try {
            votingService.castVote(selectedCandidate, selectedVoter);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
            return;
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(frame, selectedVoter.name + " has already voted!");
            return;
        }

        // Odśwież tabele i comboboksy
        refreshCandidateTable();
        refreshVoterTable();
        refreshCandidateComboBox();
        refreshVoterComboBox();
    }

    private void handleReset(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to reset the election?",
                "Reset Election",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            votingService.resetElection();
            refreshCandidateTable();
            refreshVoterTable();
            refreshCandidateComboBox();
            refreshVoterComboBox();
        }
    }

    private void refreshCandidateTable() {
        candidateTableModel.fireTableDataChanged();
    }

    private void refreshVoterTable() {
        voterTableModel.fireTableDataChanged();
    }

    private void refreshCandidateComboBox() {
        candidateComboBox.removeAllItems();
        for (Candidate c : candidateRepository.findAll()) {
            candidateComboBox.addItem(c);
        }
    }

    private void refreshVoterComboBox() {
        voterComboBox.removeAllItems();
        for (Voter v : voterRepository.findAll()) {
            voterComboBox.addItem(v);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VotingAppGUI::new);
    }
}
